package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.uruguaymnatural.backend.dto.BookingPaymentDto;
import com.uruguaymnatural.backend.dto.BookingPaymentRequestDto;
import com.uruguaymnatural.backend.dto.CardDto;
import com.uruguaymnatural.backend.dto.PaymentDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.BookingStatus;
import com.uruguaymnatural.backend.model.Card;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.repository.CardRepository;
import com.uruguaymnatural.backend.repository.PaymentRepository;
import com.uruguaymnatural.backend.service.PaymentGatewayClient.Outcome;
import com.uruguaymnatural.backend.service.PaymentGatewayClient.Result;

/**
 * Paying a booking through the external payment service (CU-16).
 *
 * Uruguay Mnatural does not process payments (RNF-10): it asks the external service and records
 * what it answered. Only an approved answer confirms the booking (RN-19); if the service rejects
 * the card, fails or does not answer in time, the booking stays pending for 24 hours (RN-21).
 */
@Service
public class PaymentService {

    private final PaymentGatewayClient gateway;
    private final PaymentRepository paymentRepository;
    private final CardRepository cardRepository;
    private final BookingService bookingService;
    private final TouristService touristService;
    private final RefundService refundService;
    private final TransactionTemplate transaction;
    private final long pendingHours;

    public PaymentService(PaymentGatewayClient gateway, PaymentRepository paymentRepository,
            CardRepository cardRepository, BookingService bookingService, TouristService touristService,
            RefundService refundService, PlatformTransactionManager transactionManager,
            @Value("${booking.pending-hours}") long pendingHours) {
        this.gateway = gateway;
        this.paymentRepository = paymentRepository;
        this.cardRepository = cardRepository;
        this.bookingService = bookingService;
        this.touristService = touristService;
        this.refundService = refundService;
        this.transaction = new TransactionTemplate(transactionManager);
        this.pendingHours = pendingHours;
    }

    /** What is needed to call the payment service, prepared in the first step. */
    private record Charge(BigDecimal amount, Card savedCard, String cardNumber, String last4) {
    }

    /**
     * Pays a booking in three steps. The call to the payment service can take up to a minute,
     * so it is made outside of any database transaction: the steps before and after it are
     * two short transactions.
     */
    public BookingPaymentDto payBooking(Long userId, Long bookingId, BookingPaymentRequestDto data) {
        // 1. Check the booking and the card. From here on the tourist "pressed pay": if the
        //    payment does not go through, the places stay held for 24 hours (RN-21).
        Charge charge = transaction.execute(status -> prepare(userId, bookingId, data));

        // 2. Ask the external service. It always comes back with an outcome, never an exception.
        Outcome outcome = charge.savedCard() != null
                ? gateway.chargeSavedCard(charge.savedCard().getGatewayToken(), charge.amount().longValue())
                : gateway.charge(charge.cardNumber(), data.holder().trim(), data.expiry(), data.cvc(),
                        charge.amount().longValue());

        // 3. Record the attempt and, only if it was approved, confirm the booking.
        return transaction.execute(status -> record(userId, bookingId, charge, outcome));
    }

    private Charge prepare(Long userId, Long bookingId, BookingPaymentRequestDto data) {
        Booking booking = bookingService.findOwn(userId, bookingId);
        LocalDateTime now = LocalDateTime.now();
        bookingService.requirePending(booking, now);
        if (!booking.hasParticipants()) {
            throw new BusinessRuleException("Antes de pagar tenés que cargar los datos de los participantes.");
        }

        Charge charge;
        if (data.cardId() != null) {
            Card card = cardRepository.findById(data.cardId())
                    .filter(found -> found.getTourist().getId().equals(booking.getTourist().getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("No encontramos esa tarjeta guardada."));
            charge = new Charge(booking.getTotal(), card, null, card.getLast4());
        } else {
            String cardNumber = checkNewCard(data);
            charge = new Charge(booking.getTotal(), null, cardNumber, cardNumber.substring(cardNumber.length() - 4));
        }

        booking.setPaymentDeadline(now.plusHours(pendingHours));
        return charge;
    }

    private BookingPaymentDto record(Long userId, Long bookingId, Charge charge, Outcome outcome) {
        Booking booking = bookingService.findOwn(userId, bookingId);
        PaymentStatus status = switch (outcome.result()) {
            case APPROVED -> PaymentStatus.APPROVED;
            case REJECTED -> PaymentStatus.REJECTED;
            case ERROR, NO_RESPONSE -> PaymentStatus.PENDING;   // not charged, not confirmed
        };
        Payment payment = new Payment(booking, charge.savedCard(), charge.amount(), status, charge.last4(),
                outcome.transactionId(), outcome.reason());
        paymentRepository.save(payment);

        if (outcome.result() == Result.APPROVED) {
            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                bookingService.confirm(booking);
                if (charge.savedCard() == null) {
                    // RF-21: a new card stays saved for the next time, as a token (RN-24).
                    payment.setCard(cardRepository.save(
                            new Card(booking.getTourist(), outcome.cardToken(), charge.last4())));
                }
            } else {
                // While we waited for the payment service the booking stopped being pending
                // (for example, the provider cancelled the date): the money goes straight back.
                refundService.refund(payment, payment.getAmount());
                payment.setMessage("La reserva ya no estaba pendiente: se devolvió el pago");
            }
        }
        return new BookingPaymentDto(PaymentDto.from(payment), bookingService.toDto(booking, LocalDateTime.now()));
    }

    /** The logged-in tourist's saved cards, newest first. */
    @Transactional(readOnly = true)
    public List<CardDto> listCards(Long userId) {
        Tourist tourist = touristService.findByUser(userId);
        return cardRepository.findByTouristIdOrderByCreatedAtDesc(tourist.getId()).stream()
                .map(CardDto::from)
                .toList();
    }

    /** Checks the data of a new card and returns its number with digits only. */
    private static String checkNewCard(BookingPaymentRequestDto data) {
        String cardNumber = data.cardNumber() == null ? "" : data.cardNumber().replaceAll("\\D", "");
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            throw new InvalidDataException("cardNumber", "El número de tarjeta tiene que tener entre 13 y 19 dígitos.");
        }
        if (data.holder() == null || data.holder().isBlank()) {
            throw new InvalidDataException("holder", "Ingresá el nombre que figura en la tarjeta.");
        }
        if (data.expiry() == null || !data.expiry().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new InvalidDataException("expiry", "Usá el formato MM/AA.");
        }
        if (isExpired(data.expiry())) {
            throw new InvalidDataException("expiry", "La tarjeta está vencida.");
        }
        if (data.cvc() == null || !data.cvc().matches("\\d{3,4}")) {
            throw new InvalidDataException("cvc", "Son 3 o 4 dígitos.");
        }
        return cardNumber;
    }

    /** A card is valid until the last day of its expiry month (MM/AA). */
    private static boolean isExpired(String expiry) {
        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3, 5));
        return YearMonth.of(year, month).isBefore(YearMonth.now());
    }
}

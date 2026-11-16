package com.uruguaymnatural.backend.service;

import static com.uruguaymnatural.backend.TestData.withId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.BookingPaymentDto;
import com.uruguaymnatural.backend.dto.BookingPaymentRequestDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.BookingParticipant;
import com.uruguaymnatural.backend.model.BookingStatus;
import com.uruguaymnatural.backend.model.Card;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.BookingRepository;
import com.uruguaymnatural.backend.repository.CardRepository;
import com.uruguaymnatural.backend.repository.PaymentRepository;
import com.uruguaymnatural.backend.service.PaymentGateway.NewCard;
import com.uruguaymnatural.backend.service.PaymentGateway.Order;
import com.uruguaymnatural.backend.service.PaymentGateway.Outcome;
import com.uruguaymnatural.backend.service.PaymentGateway.Result;

class PaymentServiceTest {

    private static final Long TOURIST_USER = 3L;

    private PaymentGateway gateway;
    private PaymentRepository paymentRepository;
    private CardRepository cardRepository;
    private EmailService emailService;
    private RefundService refundService;
    private PaymentService paymentService;

    // A pending booking of 2 places at $ 1500 with its participants loaded: $ 3000 to pay.
    private final Tourist tourist = TestData.tourist(TOURIST_USER);
    private final Activity activity = TestData.activity(1L, TestData.business(5L));
    private final ActivitySchedule schedule = TestData.schedule(10L, activity, LocalDateTime.now().plusDays(3));
    private final Booking booking = withId(
            new Booking(tourist, schedule, 2, "UM-TEST0001", LocalDateTime.now().plusMinutes(10)), 20L);

    @BeforeEach
    void setUp() {
        booking.setParticipants(List.of(
                new BookingParticipant(booking, "Ana", "Pérez", LocalDate.of(1990, 5, 20), "41234567"),
                new BookingParticipant(booking, "Luis", "Pérez", LocalDate.of(1988, 1, 2), "39876543")));

        gateway = mock(PaymentGateway.class);
        paymentRepository = mock(PaymentRepository.class);
        cardRepository = mock(CardRepository.class);
        when(cardRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        emailService = mock(EmailService.class);
        refundService = mock(RefundService.class);

        BookingRepository bookingRepository = mock(BookingRepository.class);
        when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
        TouristService touristService = mock(TouristService.class);
        // The real booking service, so the tests check the booking really ends confirmed or pending.
        BookingService bookingService = new BookingService(bookingRepository, mock(ActivityScheduleRepository.class),
                touristService, emailService, refundService, paymentRepository, 10);

        paymentService = new PaymentService(gateway, paymentRepository, cardRepository, bookingService,
                touristService, refundService, mock(PlatformTransactionManager.class), 24);
    }

    @Test
    void approvedPaymentConfirmsTheBookingSavesTheCardAndEmailsTheCode() {
        answer(new Outcome(Result.APPROVED, "sbx_123", "tok_abc", null));

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L, newCard("4111 1111 1111 1111"));

        assertEquals(PaymentStatus.APPROVED, result.payment().status());
        assertEquals(new BigDecimal("3000"), result.payment().amount());
        assertEquals(BookingStatus.CONFIRMED, result.booking().status());
        verify(emailService).send(eq(tourist.getUser().getEmail()), anyString(), anyString());
        // the service gets the number without spaces and the booking's total
        ArgumentCaptor<NewCard> sentCard = ArgumentCaptor.forClass(NewCard.class);
        ArgumentCaptor<Order> sentOrder = ArgumentCaptor.forClass(Order.class);
        verify(gateway).charge(sentCard.capture(), sentOrder.capture());
        assertEquals("4111111111111111", sentCard.getValue().number());
        assertEquals(new BigDecimal("3000"), sentOrder.getValue().amount());
        assertEquals(tourist.getUser().getEmail(), sentOrder.getValue().payerEmail());

        // RN-24: only the token and the last 4 digits are kept
        ArgumentCaptor<Card> savedCard = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(savedCard.capture());
        assertEquals("tok_abc", savedCard.getValue().getGatewayToken());
        assertEquals("1111", savedCard.getValue().getLast4());
        assertEquals("1111", savedPayment().getCardLast4());
    }

    @Test
    void rejectedPaymentLeavesTheBookingPendingFor24Hours() {
        answer(new Outcome(Result.REJECTED, null, null, "Fondos insuficientes"));

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L, newCard("4000000000000002"));

        assertEquals(PaymentStatus.REJECTED, result.payment().status());
        assertEquals("Fondos insuficientes", result.payment().message());
        assertNull(result.payment().externalReference());
        assertEquals(BookingStatus.PENDING_PAYMENT, result.booking().status());
        assertTrue(booking.getPaymentDeadline().isAfter(LocalDateTime.now().plusHours(23)));
        verify(emailService, never()).send(any(), any(), any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void whenThePaymentServiceDoesNotAnswerTheBookingIsNotConfirmed() {
        answer(new Outcome(Result.NO_RESPONSE, null, null, "El servicio de pagos no respondió a tiempo"));

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L, newCard("4000000000000408"));

        assertEquals(PaymentStatus.PENDING, result.payment().status());
        assertEquals(BookingStatus.PENDING_PAYMENT, result.booking().status());
        assertTrue(booking.getPaymentDeadline().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void whenThePaymentServiceFailsTheBookingIsNotConfirmed() {
        answer(new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error"));

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L, newCard("4000000000000500"));

        assertEquals(PaymentStatus.PENDING, result.payment().status());
        assertEquals(BookingStatus.PENDING_PAYMENT, result.booking().status());
    }

    @Test
    void payingWithASavedCardSendsItsTokenAndNotANumber() {
        Card card = withId(new Card(tourist, "tok_saved", "4242"), 7L);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));
        when(gateway.chargeSavedCard(eq("tok_saved"), eq("123"), any())).thenReturn(new Outcome(Result.APPROVED, "sbx_9", "tok_saved", null));

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L,
                new BookingPaymentRequestDto(7L, null, null, null, "123"));

        assertEquals(BookingStatus.CONFIRMED, result.booking().status());
        assertEquals("4242", result.payment().cardLast4());
        verify(gateway, never()).charge(any(), any());
        verify(cardRepository, never()).save(any());   // it was already saved
    }

    @Test
    void aSavedCardNeedsItsSecurityCode() {
        Card card = withId(new Card(tourist, "tok_saved", "4242"), 7L);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        InvalidDataException e = assertThrows(InvalidDataException.class, () -> paymentService.payBooking(
                TOURIST_USER, 20L, new BookingPaymentRequestDto(7L, null, null, null, null)));
        assertEquals("cvc", e.getField());
    }

    @Test
    void ifTheServiceCouldNotSaveTheCardThePaymentIsStillGood() {
        answer(new Outcome(Result.APPROVED, "123456", null, null));

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L, newCard("5031755734530604"));

        assertEquals(BookingStatus.CONFIRMED, result.booking().status());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void cannotPayWithAnotherTouristsSavedCard() {
        Card card = withId(new Card(TestData.tourist(8L), "tok_other", "9999"), 7L);
        when(cardRepository.findById(7L)).thenReturn(Optional.of(card));

        assertThrows(ResourceNotFoundException.class, () -> paymentService.payBooking(TOURIST_USER, 20L,
                new BookingPaymentRequestDto(7L, null, null, null, "123")));
        verify(gateway, never()).chargeSavedCard(anyString(), anyString(), any());
    }

    @Test
    void expiredCardIsRejectedBeforeCallingTheService() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String expiry = String.format("%02d/%02d", lastMonth.getMonthValue(), lastMonth.getYear() % 100);

        InvalidDataException e = assertThrows(InvalidDataException.class, () -> paymentService.payBooking(
                TOURIST_USER, 20L, new BookingPaymentRequestDto(null, "4111111111111111", "Sofía Martínez", expiry, "123")));
        assertEquals("expiry", e.getField());
        verify(gateway, never()).charge(any(), any());
    }

    @Test
    void cannotPayABookingWhoseTimeRanOut() {
        booking.setPaymentDeadline(LocalDateTime.now().minusMinutes(1));

        assertThrows(BusinessRuleException.class,
                () -> paymentService.payBooking(TOURIST_USER, 20L, newCard("4111111111111111")));
        verify(gateway, never()).charge(any(), any());
    }

    @Test
    void cannotPayBeforeLoadingTheParticipants() {
        booking.setParticipants(List.of());

        assertThrows(BusinessRuleException.class,
                () -> paymentService.payBooking(TOURIST_USER, 20L, newCard("4111111111111111")));
    }

    @Test
    void ifTheBookingWasCancelledWhileWaitingThePaymentIsGivenBack() {
        // The provider cancels the date during the call to the payment service.
        when(gateway.charge(any(), any())).thenAnswer(call -> {
            booking.cancel();
            return new Outcome(Result.APPROVED, "sbx_123", "tok_abc", null);
        });

        BookingPaymentDto result = paymentService.payBooking(TOURIST_USER, 20L, newCard("4111111111111111"));

        assertEquals(BookingStatus.CANCELLED, result.booking().status());
        verify(refundService).refund(any(Payment.class), eq(new BigDecimal("3000")));
        verify(emailService, never()).send(any(), any(), any());
    }

    private void answer(Outcome outcome) {
        when(gateway.charge(any(), any())).thenReturn(outcome);
    }

    private Payment savedPayment() {
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        return captor.getValue();
    }

    private static BookingPaymentRequestDto newCard(String cardNumber) {
        YearMonth next = YearMonth.now().plusYears(2);
        String expiry = String.format("%02d/%02d", next.getMonthValue(), next.getYear() % 100);
        return new BookingPaymentRequestDto(null, cardNumber, "Sofía Martínez", expiry, "123");
    }
}

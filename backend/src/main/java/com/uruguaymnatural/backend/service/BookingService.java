package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.BookingDto;
import com.uruguaymnatural.backend.dto.BookingRequestDto;
import com.uruguaymnatural.backend.dto.ParticipantDto;
import com.uruguaymnatural.backend.dto.ScheduleBookingsDto;
import com.uruguaymnatural.backend.dto.ScheduleDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.BookingParticipant;
import com.uruguaymnatural.backend.model.BookingStatus;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.BookingRepository;
import com.uruguaymnatural.backend.repository.PaymentRepository;

/**
 * Bookings (CU-15, CU-17, CU-18, CU-19, CU-22).
 *
 * The places work "like booking a flight" (RN-15): choosing how many places holds them for
 * the tourist for 10 minutes; nobody else can take them meanwhile.
 */
@Service
public class BookingService {

    private static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final ActivityScheduleRepository scheduleRepository;
    private final TouristService touristService;
    private final EmailService emailService;
    private final RefundService refundService;
    private final PaymentRepository paymentRepository;
    private final long holdMinutes;

    public BookingService(BookingRepository bookingRepository, ActivityScheduleRepository scheduleRepository,
            TouristService touristService, EmailService emailService, RefundService refundService,
            PaymentRepository paymentRepository, @Value("${booking.hold-minutes}") long holdMinutes) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.touristService = touristService;
        this.emailService = emailService;
        this.refundService = refundService;
        this.paymentRepository = paymentRepository;
        this.holdMinutes = holdMinutes;
    }

    // ---- The tourist books (CU-15) ----

    /**
     * Step 1: holds the places for 10 minutes. The date is read "for update": if two tourists
     * ask for the last places at the same time, the second waits for the first to finish and
     * then sees the places that are really left (RNF-12). So the places never go below zero.
     */
    @Transactional
    public BookingDto hold(Long userId, BookingRequestDto data) {
        Tourist tourist = touristService.findByUser(userId);
        ActivitySchedule schedule = scheduleRepository.findByIdForUpdate(data.scheduleId())
                .filter(found -> PortalService.isPublished(found.getActivity()))
                .orElseThrow(() -> new ResourceNotFoundException("No existe esa fecha."));
        LocalDateTime now = LocalDateTime.now();

        if (!schedule.isScheduled()) {
            throw new BusinessRuleException("Esta fecha fue cancelada.");
        }
        if (!PortalService.isInsideBookingWindow(schedule, now)) {
            throw new BusinessRuleException("Esta fecha ya no se puede reservar: está fuera del plazo que "
                    + "definió el prestador.");
        }
        int available = schedule.getAvailableCapacity();
        if (data.participantCount() > available) {
            throw new BusinessRuleException(available == 0
                    ? "No quedan lugares para esta fecha."
                    : "No hay lugares suficientes: " + (available == 1 ? "queda 1 lugar." : "quedan " + available + " lugares."));
        }

        schedule.takePlaces(data.participantCount());
        Booking booking = new Booking(tourist, schedule, data.participantCount(), newCode(),
                now.plusMinutes(holdMinutes));
        return toDto(bookingRepository.save(booking), now);
    }

    /**
     * Step 2: the data of each participant. If the activity is free the booking is confirmed
     * right here, without going through the payment (RN-09).
     */
    @Transactional
    public BookingDto saveParticipants(Long userId, Long bookingId, List<ParticipantDto> participants) {
        Booking booking = findOwn(userId, bookingId);
        LocalDateTime now = LocalDateTime.now();
        requirePending(booking, now);
        if (participants.size() != booking.getParticipantCount()) {
            throw new InvalidDataException("participants",
                    "Tenés que cargar los datos de las " + booking.getParticipantCount() + " personas.");
        }

        booking.setParticipants(participants.stream()
                .map(p -> new BookingParticipant(booking, p.firstName().trim(), p.lastName().trim(),
                        p.birthDate(), p.document().trim()))
                .toList());
        if (booking.isFree()) {
            confirm(booking);
        }
        return toDto(booking, now);
    }

    /** Marks the booking as confirmed and sends the email with its data and code (RN-23). */
    public void confirm(Booking booking) {
        booking.confirm();
        ActivitySchedule schedule = booking.getSchedule();
        emailService.send(booking.getTourist().getUser().getEmail(),
                "Tu reserva está confirmada",
                "¡Hola " + booking.getTourist().getFirstName() + "! Tu reserva quedó confirmada.\n"
                        + "Código de reserva: " + booking.getCode() + "\n"
                        + "Actividad: " + schedule.getActivity().getName() + "\n"
                        + "Fecha: " + schedule.getStart().format(DAY_AND_TIME) + "\n"
                        + "Personas: " + booking.getParticipantCount() + "\n"
                        + "Total: $ " + booking.getTotal().toPlainString());
    }

    // ---- The tourist's bookings (CU-18, CU-19) ----

    @Transactional(readOnly = true)
    public List<BookingDto> listOwn(Long userId) {
        Tourist tourist = touristService.findByUser(userId);
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findByTouristIdOrderByCreatedAtDesc(tourist.getId()).stream()
                .map(booking -> toDto(booking, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingDto getOwn(Long userId, Long bookingId) {
        return toDto(findOwn(userId, bookingId), LocalDateTime.now());
    }

    /**
     * The tourist cancels a booking (CU-19). A confirmed booking follows the activity's
     * cancellation policy (RN-26) and gets back the percentage the provider set. A booking
     * that was never paid can always be dropped.
     */
    @Transactional
    public BookingDto cancelOwn(Long userId, Long bookingId) {
        Booking booking = findOwn(userId, bookingId);
        LocalDateTime now = LocalDateTime.now();
        Cancellation cancellation = cancellationOf(booking, now);
        if (!cancellation.allowed()) {
            throw new BusinessRuleException(cancellation.note());
        }

        boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;
        booking.cancel();
        releasePlaces(booking);
        if (wasConfirmed && cancellation.refundAmount().signum() > 0) {
            refundService.refund(booking, booking.getSchedule().getActivity().getRefundPercentage());
        }
        return toDto(booking, now);
    }

    // ---- Cancelled by the provider or the administrator (RN-27, RN-28) ----

    /** Cancels every booking of a date that was cancelled and tells each tourist by email. */
    @Transactional
    public void cancelAllOf(ActivitySchedule schedule, String reason) {
        for (Booking booking : bookingRepository.findByScheduleIdAndStatusIn(schedule.getId(), ACTIVE)) {
            boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;
            booking.cancel();
            // RN-27, RN-28: when it is not the tourist who cancels, everything paid goes back.
            BigDecimal refunded = wasConfirmed ? refundService.refund(booking, 100) : BigDecimal.ZERO;
            emailService.send(booking.getTourist().getUser().getEmail(),
                    "Se canceló tu reserva",
                    "Hola " + booking.getTourist().getFirstName() + ". Lamentablemente se canceló \""
                            + schedule.getActivity().getName() + "\" del "
                            + schedule.getStart().format(DAY_AND_TIME) + " porque " + reason + ".\n"
                            + "Tu reserva " + booking.getCode() + " quedó cancelada."
                            + (refunded.signum() > 0
                                    ? "\nTe devolvimos el 100 % de lo que pagaste: $ " + refunded.toPlainString() + "."
                                    : ""));
        }
    }

    // ---- The provider sees who booked (CU-22) ----

    @Transactional(readOnly = true)
    public ScheduleBookingsDto listOfSchedule(Long userId, Long scheduleId) {
        ActivitySchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la fecha " + scheduleId + "."));
        Activity activity = schedule.getActivity();
        if (!activity.getBusiness().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Esta actividad es de otro emprendimiento.");
        }
        List<ScheduleBookingsDto.Item> items = bookingRepository.findByScheduleIdOrderByCreatedAtAsc(scheduleId)
                .stream()
                .map(booking -> new ScheduleBookingsDto.Item(
                        booking.getId(),
                        booking.getCode(),
                        booking.getStatus(),
                        booking.getTourist().getFullName(),
                        booking.getTourist().getUser().getEmail(),
                        booking.getParticipantCount(),
                        booking.getTotal(),
                        booking.getParticipants().stream().map(ParticipantDto::from).toList()))
                .toList();
        return new ScheduleBookingsDto(activity.getName(), ScheduleDto.from(schedule), items);
    }

    // ---- Automatic expiry (CU-17) ----

    /**
     * Expires the pending bookings whose time ran out and releases their places: the 10
     * minutes of a booking the tourist never paid, or the 24 hours after a failed payment.
     * Returns how many expired.
     */
    @Transactional
    public int expireOverdue() {
        List<Booking> overdue = bookingRepository.findByStatusAndPaymentDeadlineBefore(
                BookingStatus.PENDING_PAYMENT, LocalDateTime.now());
        for (Booking booking : overdue) {
            booking.expire();
            releasePlaces(booking);
        }
        return overdue.size();
    }

    // ---- Helpers ----

    /** The booking, only if it belongs to the logged-in tourist (RNF-16). */
    public Booking findOwn(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la reserva " + bookingId + "."));
        if (!booking.getTourist().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Esta reserva es de otro usuario.");
        }
        return booking;
    }

    /** The booking must still be waiting for the payment, with its time not run out. */
    public void requirePending(Booking booking, LocalDateTime now) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("Esta reserva ya no está pendiente.");
        }
        if (now.isAfter(booking.getPaymentDeadline())) {
            throw new BusinessRuleException(
                    "Se venció el tiempo para completar la reserva y los lugares se liberaron. Empezá de nuevo.");
        }
    }

    /** Gives the places back to the date, unless the whole date was cancelled. */
    private void releasePlaces(Booking booking) {
        scheduleRepository.findByIdForUpdate(booking.getSchedule().getId())
                .filter(ActivitySchedule::isScheduled)
                .ifPresent(schedule -> schedule.releasePlaces(booking.getParticipantCount()));
    }

    /** Whether the tourist can cancel now and how much they get back. */
    public record Cancellation(boolean allowed, String note, BigDecimal refundAmount) {
    }

    public Cancellation cancellationOf(Booking booking, LocalDateTime now) {
        if (!booking.isActive()) {
            return new Cancellation(false, "Esta reserva ya no está activa.", BigDecimal.ZERO);
        }
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            return new Cancellation(true, "Todavía no pagaste, así que no hay nada que devolver.", BigDecimal.ZERO);
        }

        Activity activity = booking.getSchedule().getActivity();
        LocalDateTime start = booking.getSchedule().getStart();
        if (!now.isBefore(start)) {
            return new Cancellation(false, "La actividad ya empezó.", BigDecimal.ZERO);
        }
        if (!activity.isCancellable()) {
            return new Cancellation(false, "Esta actividad no se puede cancelar.", BigDecimal.ZERO);
        }
        if (now.isAfter(start.minusMinutes(activity.getCancellationMinutes()))) {
            return new Cancellation(false, "Ya pasó el plazo para cancelar esta reserva.", BigDecimal.ZERO);
        }
        if (booking.isFree()) {
            return new Cancellation(true, "La actividad no tenía costo.", BigDecimal.ZERO);
        }
        if (!activity.isRefundable() || activity.getRefundPercentage() == 0) {
            return new Cancellation(true, "Esta actividad no tiene reembolso: no se te devuelve nada.", BigDecimal.ZERO);
        }
        BigDecimal refund = percentageOf(booking.getTotal(), activity.getRefundPercentage());
        return new Cancellation(true,
                "Se te devuelve el " + activity.getRefundPercentage() + " % de lo que pagaste.", refund);
    }

    public static BigDecimal percentageOf(BigDecimal amount, int percentage) {
        return amount.multiply(BigDecimal.valueOf(percentage)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String newCode() {
        String code;
        do {
            code = "UM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.existsByCode(code));
        return code;
    }

    /** For a pending booking: why its last payment attempt did not go through, if there was one. */
    private String lastPaymentProblem(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT || booking.getId() == null) {
            return null;
        }
        return paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(booking.getId())
                .filter(payment -> payment.getStatus() != PaymentStatus.APPROVED)
                .map(Payment::getMessage)
                .orElse(null);
    }

    public BookingDto toDto(Booking booking, LocalDateTime now) {
        ActivitySchedule schedule = booking.getSchedule();
        Activity activity = schedule.getActivity();
        Cancellation cancellation = cancellationOf(booking, now);
        // RN-31: the tourist took part if the booking is confirmed and the date already passed.
        boolean tookPart = booking.getStatus() == BookingStatus.CONFIRMED && schedule.getStart().isBefore(now);
        return new BookingDto(
                booking.getId(),
                booking.getCode(),
                booking.getStatus(),
                activity.getId(),
                activity.getName(),
                activity.getBusiness().getLegalName(),
                schedule.getDate(),
                schedule.getStartTime(),
                booking.getParticipantCount(),
                schedule.getPrice(),
                booking.getTotal(),
                booking.getCreatedAt(),
                booking.getPaymentDeadline(),
                booking.getParticipants().stream().map(ParticipantDto::from).toList(),
                cancellation.allowed(),
                cancellation.note(),
                cancellation.refundAmount(),
                lastPaymentProblem(booking),
                tookPart && activity.isActive());
    }
}

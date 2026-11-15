package com.uruguaymnatural.backend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.test.util.ReflectionTestUtils;

import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.PersonType;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.Tag;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.model.User;

/**
 * Ready-made objects for the tests, so each test only writes what matters to it.
 */
public final class TestData {

    private TestData() {
    }

    public static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    /** An approved business whose provider has the given user id. */
    public static Business business(Long userId) {
        User user = withId(new User("prestador" + userId + "@mail.com", "hash", Role.PROVIDER, true), userId);
        Business business = new Business(user, PersonType.INDIVIDUAL, "12345678", "Cabalgatas Ana", "099123456",
                user.getEmail(), null, "Cabalgatas por la sierra", "Rocha", "La Paloma", null);
        business.approve();
        return withId(business, userId * 10);
    }

    public static Tourist tourist(Long userId) {
        User user = withId(new User("turista" + userId + "@mail.com", "hash", Role.TOURIST, true), userId);
        return withId(new Tourist(user, "Sofía", "Martínez", "Uruguay", LocalDate.of(1995, 3, 10),
                "Español", "41234567"), userId * 10);
    }

    /**
     * A published activity: 10 places, $ 1500 per person, bookings until the start,
     * cancellable until 24 hours before with a 50 % refund.
     */
    public static Activity activity(Long id, Business business) {
        Activity activity = new Activity(business);
        activity.setDescriptionData("Cabalgata por la sierra", "Dos horas a caballo", null, null, 120);
        activity.setLocation("Rocha", "La Paloma", null, null, null);
        activity.setBookingRules(10, new BigDecimal("1500"), 0, null);
        activity.setCancellationPolicy(true, 1440, true, 50);
        activity.setTags(Set.of(withId(new Tag("Cabalgata"), 2L)));
        activity.setEquipment(List.of(), List.of());
        return withId(activity, id);
    }

    /** A date of the activity that starts at the given moment, with the activity's places and price. */
    public static ActivitySchedule schedule(Long id, Activity activity, LocalDateTime start) {
        return withId(new ActivitySchedule(activity, start.toLocalDate(), start.toLocalTime(),
                activity.getDefaultCapacity(), activity.getDefaultPrice()), id);
    }
}

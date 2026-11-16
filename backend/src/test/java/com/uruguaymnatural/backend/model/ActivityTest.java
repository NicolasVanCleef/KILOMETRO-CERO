package com.uruguaymnatural.backend.model;

import static com.uruguaymnatural.backend.TestData.withId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

class ActivityTest {

    private final Equipment helmet = withId(new Equipment("Casco"), 1L);
    private final Equipment water = withId(new Equipment("Agua"), 2L);
    private final Equipment sunscreen = withId(new Equipment("Protector solar"), 3L);

    @Test
    void savingTheSameEquipmentAgainKeepsItsRow() {
        Activity activity = new Activity(null);
        activity.setEquipment(List.of(water), List.of(helmet));
        ActivityEquipment waterRow = rowOf(activity, water);

        // Same equipment as before: the provider just edited another field.
        activity.setEquipment(List.of(water), List.of(helmet));

        assertEquals(2, activity.getEquipment().size());
        assertSame(waterRow, rowOf(activity, water));
    }

    @Test
    void changingTheEquipmentKeepsRemovesAndAdds() {
        Activity activity = new Activity(null);
        activity.setEquipment(List.of(water), List.of(helmet));
        ActivityEquipment helmetRow = rowOf(activity, helmet);

        // The helmet becomes required, the water is removed and the sunscreen is new.
        activity.setEquipment(List.of(helmet), List.of(sunscreen));

        assertEquals(2, activity.getEquipment().size());
        assertSame(helmetRow, rowOf(activity, helmet));
        assertEquals(EquipmentRelation.REQUIRED, helmetRow.getRelation());
        assertEquals(EquipmentRelation.PROVIDED, rowOf(activity, sunscreen).getRelation());
    }

    private static ActivityEquipment rowOf(Activity activity, Equipment item) {
        return activity.getEquipment().stream()
                .filter(row -> row.getEquipment() == item)
                .findFirst()
                .orElseThrow();
    }
}

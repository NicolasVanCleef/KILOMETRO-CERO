package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.model.Equipment;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.EquipmentRepository;

class EquipmentServiceTest {

    private EquipmentRepository equipmentRepository;
    private ActivityRepository activityRepository;
    private EquipmentService equipmentService;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        when(equipmentRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        equipmentService = new EquipmentService(equipmentRepository, activityRepository);
    }

    @Test
    void createsEquipment() {
        assertEquals("Casco", equipmentService.create("Casco").name());
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void repeatedNameIsRejected() {
        Equipment helmet = new Equipment("Casco");
        ReflectionTestUtils.setField(helmet, "id", 1L);
        when(equipmentRepository.findByNameIgnoreCase("CASCO")).thenReturn(Optional.of(helmet));

        assertThrows(BusinessRuleException.class, () -> equipmentService.create("CASCO"));
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void deletesEquipment() {
        Equipment helmet = new Equipment("Casco");
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(helmet));

        equipmentService.delete(1L);

        verify(equipmentRepository).delete(helmet);
    }

    @Test
    void equipmentUsedByPublishedActivityCannotBeDeleted() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(new Equipment("Casco")));
        when(activityRepository.existsByEquipmentEquipmentIdAndActiveTrue(1L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> equipmentService.delete(1L));
        verify(equipmentRepository, never()).delete(any());
    }
}

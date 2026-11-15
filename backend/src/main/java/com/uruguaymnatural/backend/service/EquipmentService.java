package com.uruguaymnatural.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.CatalogItemDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Equipment;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.EquipmentRepository;

/**
 * The administrator's catalogue of equipment types (CU-09, RF-43).
 */
@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ActivityRepository activityRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, ActivityRepository activityRepository) {
        this.equipmentRepository = equipmentRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemDto> list() {
        return equipmentRepository.findAllByOrderByNameAsc().stream().map(CatalogItemDto::from).toList();
    }

    @Transactional
    public CatalogItemDto create(String name) {
        String cleanName = name.trim();
        checkNameIsFree(cleanName, null);
        return CatalogItemDto.from(equipmentRepository.save(new Equipment(cleanName)));
    }

    @Transactional
    public CatalogItemDto rename(Long id, String name) {
        Equipment equipment = find(id);
        String cleanName = name.trim();
        checkNameIsFree(cleanName, id);
        equipment.setName(cleanName);
        return CatalogItemDto.from(equipment);
    }

    /**
     * Deletes an equipment type. Not allowed while a published activity uses it.
     * Activities already deleted simply lose it.
     */
    @Transactional
    public void delete(Long id) {
        Equipment equipment = find(id);
        if (activityRepository.existsByEquipmentEquipmentIdAndActiveTrue(id)) {
            throw new BusinessRuleException(
                    "Hay actividades publicadas que usan \"" + equipment.getName() + "\". No se puede eliminar.");
        }
        activityRepository.findByEquipmentEquipmentId(id).forEach(activity ->
                activity.getEquipment().removeIf(item -> item.getEquipment().getId().equals(id)));
        equipmentRepository.delete(equipment);
    }

    private Equipment find(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el equipamiento " + id + "."));
    }

    /** Two types of equipment cannot have the same name (CU-09, 3c). */
    private void checkNameIsFree(String name, Long ownId) {
        equipmentRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(ownId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException(
                            "Ya existe un equipamiento con el nombre \"" + existing.getName() + "\".");
                });
    }
}

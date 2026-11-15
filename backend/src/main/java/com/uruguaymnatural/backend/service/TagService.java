package com.uruguaymnatural.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.CatalogItemDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Tag;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.TagRepository;

/**
 * The administrator's catalogue of activity types (CU-08, RF-42).
 */
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ActivityRepository activityRepository;

    public TagService(TagRepository tagRepository, ActivityRepository activityRepository) {
        this.tagRepository = tagRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemDto> list() {
        return tagRepository.findAllByOrderByNameAsc().stream().map(CatalogItemDto::from).toList();
    }

    @Transactional
    public CatalogItemDto create(String name) {
        String cleanName = name.trim();
        checkNameIsFree(cleanName, null);
        return CatalogItemDto.from(tagRepository.save(new Tag(cleanName)));
    }

    @Transactional
    public CatalogItemDto rename(Long id, String name) {
        Tag tag = find(id);
        String cleanName = name.trim();
        checkNameIsFree(cleanName, id);
        tag.setName(cleanName);
        return CatalogItemDto.from(tag);
    }

    /**
     * Deletes a tag. Not allowed while a published activity uses it, because every activity
     * needs at least one tag (RN-05). Activities already deleted simply lose the tag.
     */
    @Transactional
    public void delete(Long id) {
        Tag tag = find(id);
        if (activityRepository.existsByTagsIdAndActiveTrue(id)) {
            throw new BusinessRuleException(
                    "Hay actividades publicadas con la etiqueta \"" + tag.getName() + "\". No se puede eliminar.");
        }
        activityRepository.findByTagsId(id).forEach(activity -> activity.getTags().remove(tag));
        tagRepository.delete(tag);
    }

    private Tag find(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la etiqueta " + id + "."));
    }

    /** Two tags cannot have the same name, even with different capital letters (CU-08, 3c). */
    private void checkNameIsFree(String name, Long ownId) {
        tagRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(ownId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ya existe una etiqueta con el nombre \"" + existing.getName() + "\".");
                });
    }
}

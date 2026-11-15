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

import com.uruguaymnatural.backend.dto.CatalogItemDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Tag;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.TagRepository;

class TagServiceTest {

    private TagRepository tagRepository;
    private ActivityRepository activityRepository;
    private TagService tagService;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        tagRepository = mock(TagRepository.class);
        when(tagRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        tagService = new TagService(tagRepository, activityRepository);
    }

    @Test
    void createsTagWithTrimmedName() {
        CatalogItemDto result = tagService.create("  Trekking ");

        assertEquals("Trekking", result.name());
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void repeatedNameIsRejectedIgnoringCapitals() {
        when(tagRepository.findByNameIgnoreCase("trekking")).thenReturn(Optional.of(tag(1L, "Trekking")));

        assertThrows(BusinessRuleException.class, () -> tagService.create("trekking"));
        verify(tagRepository, never()).save(any());
    }

    @Test
    void renamesTag() {
        Tag tag = tag(1L, "Treking");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        tagService.rename(1L, "Trekking");

        assertEquals("Trekking", tag.getName());
    }

    @Test
    void renamingToItsOwnNameIsAllowed() {
        Tag tag = tag(1L, "Trekking");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.findByNameIgnoreCase("TREKKING")).thenReturn(Optional.of(tag));

        assertEquals("TREKKING", tagService.rename(1L, "TREKKING").name());
    }

    @Test
    void renamingToAnotherTagsNameIsRejected() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag(1L, "Trekking")));
        when(tagRepository.findByNameIgnoreCase("Cabalgata")).thenReturn(Optional.of(tag(2L, "Cabalgata")));

        assertThrows(BusinessRuleException.class, () -> tagService.rename(1L, "Cabalgata"));
    }

    @Test
    void deletingUnknownTagIsNotFound() {
        when(tagRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tagService.delete(9L));
    }

    @Test
    void tagUsedByPublishedActivityCannotBeDeleted() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag(1L, "Trekking")));
        when(activityRepository.existsByTagsIdAndActiveTrue(1L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> tagService.delete(1L));
        verify(tagRepository, never()).delete(any());
    }

    private static Tag tag(Long id, String name) {
        Tag tag = new Tag(name);
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }
}

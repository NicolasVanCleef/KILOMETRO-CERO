package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.CatalogItemDto;
import com.uruguaymnatural.backend.dto.CatalogItemRequestDto;
import com.uruguaymnatural.backend.service.EquipmentService;
import com.uruguaymnatural.backend.service.TagService;

import jakarta.validation.Valid;

/**
 * Tags and equipment types. Anyone can read the lists (the portal filters by tag and the
 * providers pick from them); only the administrator can change them (RN-06).
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final TagService tagService;
    private final EquipmentService equipmentService;

    public CatalogController(TagService tagService, EquipmentService equipmentService) {
        this.tagService = tagService;
        this.equipmentService = equipmentService;
    }

    // ---- Tags (CU-08) ----

    /** GET /api/tags — all the tags, by name. */
    @GetMapping("/tags")
    public List<CatalogItemDto> listTags() {
        return tagService.list();
    }

    /** POST /api/admin/tags — creates a tag. 201, or 409 if the name is taken. */
    @PostMapping("/admin/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogItemDto createTag(@Valid @RequestBody CatalogItemRequestDto data) {
        return tagService.create(data.name());
    }

    /** PUT /api/admin/tags/{id} — renames a tag. */
    @PutMapping("/admin/tags/{id}")
    public CatalogItemDto renameTag(@PathVariable Long id, @Valid @RequestBody CatalogItemRequestDto data) {
        return tagService.rename(id, data.name());
    }

    /** DELETE /api/admin/tags/{id} — deletes a tag. 204. */
    @DeleteMapping("/admin/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        tagService.delete(id);
    }

    // ---- Equipment (CU-09) ----

    /** GET /api/equipment — all the equipment types, by name. */
    @GetMapping("/equipment")
    public List<CatalogItemDto> listEquipment() {
        return equipmentService.list();
    }

    /** POST /api/admin/equipment — creates an equipment type. 201, or 409 if the name is taken. */
    @PostMapping("/admin/equipment")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogItemDto createEquipment(@Valid @RequestBody CatalogItemRequestDto data) {
        return equipmentService.create(data.name());
    }

    /** PUT /api/admin/equipment/{id} — renames an equipment type. */
    @PutMapping("/admin/equipment/{id}")
    public CatalogItemDto renameEquipment(@PathVariable Long id, @Valid @RequestBody CatalogItemRequestDto data) {
        return equipmentService.rename(id, data.name());
    }

    /** DELETE /api/admin/equipment/{id} — deletes an equipment type. 204. */
    @DeleteMapping("/admin/equipment/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquipment(@PathVariable Long id) {
        equipmentService.delete(id);
    }
}

package com.uruguaymnatural.backend.dto;

import com.uruguaymnatural.backend.model.Equipment;
import com.uruguaymnatural.backend.model.Tag;

/**
 * One tag or one type of equipment: both are just an id and a name.
 */
public record CatalogItemDto(Long id, String name) {

    public static CatalogItemDto from(Tag tag) {
        return new CatalogItemDto(tag.getId(), tag.getName());
    }

    public static CatalogItemDto from(Equipment equipment) {
        return new CatalogItemDto(equipment.getId(), equipment.getName());
    }
}

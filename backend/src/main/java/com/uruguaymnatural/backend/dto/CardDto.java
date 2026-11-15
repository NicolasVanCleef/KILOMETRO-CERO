package com.uruguaymnatural.backend.dto;

import com.uruguaymnatural.backend.model.Card;

/**
 * A saved card as the tourist sees it: only its last 4 digits (RNF-17).
 */
public record CardDto(Long id, String last4) {

    public static CardDto from(Card card) {
        return new CardDto(card.getId(), card.getLast4());
    }
}

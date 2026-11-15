package com.uruguaymnatural.backend.dto;

/**
 * Answer of a successful login: the token to send in the next requests and who logged in.
 */
public record SessionDto(String token, UserDto user) {
}

package com.turbo.tracker.dto.response;

public record AuthResponse(String token, UserResponse user) {
}

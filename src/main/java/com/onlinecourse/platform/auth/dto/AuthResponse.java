package com.onlinecourse.platform.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String role
) {}

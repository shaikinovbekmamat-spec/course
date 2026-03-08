package com.onlinecourse.platform.enrollment.dto;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String enrolledAt
) {}

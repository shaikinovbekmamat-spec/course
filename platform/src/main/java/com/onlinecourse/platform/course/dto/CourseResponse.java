package com.onlinecourse.platform.course.dto;

public record CourseResponse(
        Long id,
        String title,
        String description,
        InstructorInfo instructor,
        CategoryResponse category,
        Boolean published,
        Long lessonCount,
        String createdAt
) {}

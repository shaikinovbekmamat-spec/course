package com.onlinecourse.platform.lesson.dto;

public record LessonResponse(
        Long id,
        String title,
        String content,
        Integer orderIndex
) {}

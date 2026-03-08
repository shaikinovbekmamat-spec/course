package com.onlinecourse.platform.enrollment.dto;

public record LessonProgressInfo(
        Long lessonId,
        String lessonTitle,
        boolean completed,
        String completedAt
) {}

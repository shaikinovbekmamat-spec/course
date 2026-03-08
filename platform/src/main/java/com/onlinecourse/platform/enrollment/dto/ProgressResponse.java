package com.onlinecourse.platform.enrollment.dto;

import java.util.List;

public record ProgressResponse(
        Long courseId,
        String courseTitle,
        long totalLessons,
        long completedLessons,
        double percent,
        List<LessonProgressInfo> lessons
) {}

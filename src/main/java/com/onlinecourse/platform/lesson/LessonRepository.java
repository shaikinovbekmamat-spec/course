package com.onlinecourse.platform.lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByCourse_IdOrderByOrderIndexAsc(Long courseId);
    long countByCourse_Id(Long courseId);
}

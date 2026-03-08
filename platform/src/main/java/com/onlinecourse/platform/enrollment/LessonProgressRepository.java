package com.onlinecourse.platform.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findAllByEnrollment_Id(Long enrollmentId);
    boolean existsByEnrollment_IdAndLesson_Id(Long enrollmentId, Long lessonId);
    Optional<LessonProgress> findByEnrollment_IdAndLesson_Id(Long enrollmentId, Long lessonId);
    long countByEnrollment_Id(Long enrollmentId);
}

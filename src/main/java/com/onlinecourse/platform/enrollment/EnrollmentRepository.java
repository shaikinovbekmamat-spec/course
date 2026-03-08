package com.onlinecourse.platform.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findAllByStudent_Id(Long studentId);
    Optional<Enrollment> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);
    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);
}

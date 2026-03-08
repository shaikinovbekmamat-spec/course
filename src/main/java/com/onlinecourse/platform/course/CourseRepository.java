package com.onlinecourse.platform.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByPublishedTrue();

    List<Course> findAllByPublishedTrueAndCategory_Slug(String slug);

    List<Course> findAllByInstructor_Id(Long instructorId);

    @Query("SELECT c FROM Course c WHERE c.id = :id AND (c.published = true OR c.instructor.id = :userId)")
    Optional<Course> findByIdVisibleToUser(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId")
    long countLessonsByCourseId(@Param("courseId") Long courseId);
}

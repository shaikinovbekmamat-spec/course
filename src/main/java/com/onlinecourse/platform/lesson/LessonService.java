package com.onlinecourse.platform.lesson;

import com.onlinecourse.platform.course.Course;
import com.onlinecourse.platform.course.CourseRepository;
import com.onlinecourse.platform.enrollment.EnrollmentRepository;
import com.onlinecourse.platform.exception.AccessDeniedException;
import com.onlinecourse.platform.exception.ResourceNotFoundException;
import com.onlinecourse.platform.lesson.dto.LessonRequest;
import com.onlinecourse.platform.lesson.dto.LessonResponse;
import com.onlinecourse.platform.user.Role;
import com.onlinecourse.platform.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<LessonResponse> getLessons(Long courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check access: must be enrolled student, the course instructor, or admin
        boolean isInstructor = course.getInstructor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isEnrolled = enrollmentRepository.existsByStudent_IdAndCourse_Id(currentUser.getId(), courseId);

        if (!isInstructor && !isAdmin && !isEnrolled) {
            throw new AccessDeniedException("You must be enrolled in this course to view lessons");
        }

        return lessonRepository.findAllByCourse_IdOrderByOrderIndexAsc(courseId)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LessonResponse addLesson(Long courseId, LessonRequest request, User currentUser) {
        Course course = findOwnedCourse(courseId, currentUser);

        Lesson lesson = Lesson.builder()
                .course(course)
                .title(request.title())
                .content(request.content())
                .orderIndex(request.orderIndex() != null ? request.orderIndex() : 0)
                .build();

        return toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse updateLesson(Long courseId, Long lessonId, LessonRequest request, User currentUser) {
        findOwnedCourse(courseId, currentUser);
        Lesson lesson = findLessonInCourse(lessonId, courseId);

        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }

        return toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public void deleteLesson(Long courseId, Long lessonId, User currentUser) {
        findOwnedCourse(courseId, currentUser);
        Lesson lesson = findLessonInCourse(lessonId, courseId);
        lessonRepository.delete(lesson);
    }

    private Course findOwnedCourse(Long courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        if (currentUser.getRole() != Role.ADMIN &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to modify lessons in this course");
        }
        return course;
    }

    private Lesson findLessonInCourse(Long lessonId, Long courseId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));
        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Lesson " + lessonId + " is not part of course " + courseId);
        }
        return lesson;
    }

    private LessonResponse toResponse(Lesson l) {
        return new LessonResponse(l.getId(), l.getTitle(), l.getContent(), l.getOrderIndex());
    }
}

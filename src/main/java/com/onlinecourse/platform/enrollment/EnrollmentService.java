package com.onlinecourse.platform.enrollment;

import com.onlinecourse.platform.course.Course;
import com.onlinecourse.platform.course.CourseRepository;
import com.onlinecourse.platform.enrollment.dto.EnrollmentResponse;
import com.onlinecourse.platform.enrollment.dto.LessonProgressInfo;
import com.onlinecourse.platform.enrollment.dto.ProgressResponse;
import com.onlinecourse.platform.exception.AccessDeniedException;
import com.onlinecourse.platform.exception.BusinessException;
import com.onlinecourse.platform.exception.ResourceNotFoundException;
import com.onlinecourse.platform.lesson.Lesson;
import com.onlinecourse.platform.lesson.LessonRepository;
import com.onlinecourse.platform.user.Role;
import com.onlinecourse.platform.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public EnrollmentResponse enroll(Long courseId, User student) {
        if (student.getRole() == Role.INSTRUCTOR) {
            throw new BusinessException("Instructors cannot enroll in courses");
        }

        Course course = courseRepository.findById(courseId)
                .filter(c -> c.getPublished())
                .orElseThrow(() -> new ResourceNotFoundException("Published course not found with id: " + courseId));

        if (enrollmentRepository.existsByStudent_IdAndCourse_Id(student.getId(), courseId)) {
            throw new BusinessException("You are already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return toEnrollmentResponse(saved);
    }

    public List<EnrollmentResponse> getMyEnrollments(User student) {
        return enrollmentRepository.findAllByStudent_Id(student.getId())
                .stream().map(this::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    public ProgressResponse getProgress(Long courseId, User student) {
        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new AccessDeniedException("You are not enrolled in this course"));

        List<Lesson> lessons = lessonRepository.findAllByCourse_IdOrderByOrderIndexAsc(courseId);
        List<LessonProgress> progresses = lessonProgressRepository.findAllByEnrollment_Id(enrollment.getId());

        Map<Long, LessonProgress> progressMap = progresses.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p));

        List<LessonProgressInfo> lessonInfos = lessons.stream()
                .map(l -> {
                    LessonProgress lp = progressMap.get(l.getId());
                    return new LessonProgressInfo(
                            l.getId(),
                            l.getTitle(),
                            lp != null,
                            lp != null ? lp.getCompletedAt().toString() : null
                    );
                }).collect(Collectors.toList());

        long total = lessons.size();
        long completed = progresses.size();
        double percent = total > 0 ? Math.round((completed * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return new ProgressResponse(
                courseId,
                enrollment.getCourse().getTitle(),
                total,
                completed,
                percent,
                lessonInfos
        );
    }

    @Transactional
    public void completeLesson(Long courseId, Long lessonId, User student) {
        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new AccessDeniedException("You are not enrolled in this course"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Lesson does not belong to this course");
        }

        if (lessonProgressRepository.existsByEnrollment_IdAndLesson_Id(enrollment.getId(), lessonId)) {
            throw new BusinessException("Lesson is already marked as completed");
        }

        LessonProgress progress = LessonProgress.builder()
                .enrollment(enrollment)
                .lesson(lesson)
                .build();

        lessonProgressRepository.save(progress);
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment e) {
        return new EnrollmentResponse(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getTitle(),
                e.getEnrolledAt().toString()
        );
    }
}

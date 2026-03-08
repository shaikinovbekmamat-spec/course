package com.onlinecourse.platform.course;

import com.onlinecourse.platform.course.dto.*;
import com.onlinecourse.platform.exception.AccessDeniedException;
import com.onlinecourse.platform.exception.ResourceNotFoundException;
import com.onlinecourse.platform.user.User;
import com.onlinecourse.platform.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    public List<CourseResponse> getAllPublishedCourses(String categorySlug) {
        List<Course> courses;
        if (categorySlug != null && !categorySlug.isBlank()) {
            courses = courseRepository.findAllByPublishedTrueAndCategory_Slug(categorySlug);
        } else {
            courses = courseRepository.findAllByPublishedTrue();
        }
        return courses.stream().map(c -> toResponse(c, courseRepository.countLessonsByCourseId(c.getId())))
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(Long id, User currentUser) {
        Course course;
        if (currentUser != null) {
            course = courseRepository.findByIdVisibleToUser(id, currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        } else {
            course = courseRepository.findById(id)
                    .filter(c -> c.getPublished())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        }
        return toResponse(course, courseRepository.countLessonsByCourseId(id));
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request, User instructor) {
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));
        }

        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .instructor(instructor)
                .category(category)
                .published(false)
                .build();

        return toResponse(courseRepository.save(course), 0L);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request, User currentUser) {
        Course course = findOwnedCourse(id, currentUser);

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));
        }

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCategory(category);

        return toResponse(courseRepository.save(course), courseRepository.countLessonsByCourseId(id));
    }

    @Transactional
    public void deleteCourse(Long id, User currentUser) {
        Course course = findOwnedCourse(id, currentUser);
        courseRepository.delete(course);
    }

    @Transactional
    public CourseResponse togglePublish(Long id, User currentUser) {
        Course course = findOwnedCourse(id, currentUser);
        course.setPublished(!course.getPublished());
        return toResponse(courseRepository.save(course), courseRepository.countLessonsByCourseId(id));
    }

    private Course findOwnedCourse(Long id, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        if (currentUser.getRole() != Role.ADMIN &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this course");
        }
        return course;
    }

    public CourseResponse toResponse(Course c, long lessonCount) {
        InstructorInfo instructorInfo = new InstructorInfo(
                c.getInstructor().getId(),
                c.getInstructor().getFirstName(),
                c.getInstructor().getLastName(),
                c.getInstructor().getEmail()
        );
        CategoryResponse categoryResponse = c.getCategory() != null
                ? new CategoryResponse(c.getCategory().getId(), c.getCategory().getName(), c.getCategory().getSlug())
                : null;

        return new CourseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                instructorInfo,
                categoryResponse,
                c.getPublished(),
                lessonCount,
                c.getCreatedAt().toString()
        );
    }
}

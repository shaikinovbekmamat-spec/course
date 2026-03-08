package com.onlinecourse.platform.lesson;

import com.onlinecourse.platform.lesson.dto.LessonRequest;
import com.onlinecourse.platform.lesson.dto.LessonResponse;
import com.onlinecourse.platform.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/lessons")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lessons", description = "Course lessons management")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Get all lessons of a course (enrolled student or instructor)")
    public ResponseEntity<List<LessonResponse>> getAll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(lessonService.getLessons(courseId, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Add a lesson to course (course owner or ADMIN)")
    public ResponseEntity<LessonResponse> add(
            @PathVariable Long courseId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.addLesson(courseId, request, currentUser));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update a lesson (course owner or ADMIN)")
    public ResponseEntity<LessonResponse> update(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(lessonService.updateLesson(courseId, lessonId, request, currentUser));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete a lesson (course owner or ADMIN)")
    public ResponseEntity<Void> delete(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User currentUser) {
        lessonService.deleteLesson(courseId, lessonId, currentUser);
        return ResponseEntity.noContent().build();
    }
}

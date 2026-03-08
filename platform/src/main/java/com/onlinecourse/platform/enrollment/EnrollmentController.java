package com.onlinecourse.platform.enrollment;

import com.onlinecourse.platform.enrollment.dto.EnrollmentResponse;
import com.onlinecourse.platform.enrollment.dto.ProgressResponse;
import com.onlinecourse.platform.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Enrollments", description = "Student course enrollment and progress")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/{courseId}")
    @Operation(summary = "Enroll in a course (STUDENT only)")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enroll(courseId, currentUser));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(currentUser));
    }

    @GetMapping("/{courseId}/progress")
    @Operation(summary = "Get my progress in a course")
    public ResponseEntity<ProgressResponse> getProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(enrollmentService.getProgress(courseId, currentUser));
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/complete")
    @Operation(summary = "Mark a lesson as completed")
    public ResponseEntity<Void> completeLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User currentUser) {
        enrollmentService.completeLesson(courseId, lessonId, currentUser);
        return ResponseEntity.ok().build();
    }
}

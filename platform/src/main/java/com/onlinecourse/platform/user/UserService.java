package com.onlinecourse.platform.user;

import com.onlinecourse.platform.user.dto.UpdateProfileRequest;
import com.onlinecourse.platform.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(User currentUser) {
        return toResponse(currentUser);
    }

    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        currentUser.setFirstName(request.firstName());
        currentUser.setLastName(request.lastName());
        userRepository.save(currentUser);
        return toResponse(currentUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
}

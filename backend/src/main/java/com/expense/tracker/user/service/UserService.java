package com.expense.tracker.user.service;

import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.user.dto.ProfileResponse;
import com.expense.tracker.user.dto.UpdateProfileRequest;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(String email) {
        return toResponse(getUser(email));
    }

    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUser(email);
        user.setFullName(request.fullName());
        return toResponse(userRepository.save(user));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getCreatedAt());
    }
}

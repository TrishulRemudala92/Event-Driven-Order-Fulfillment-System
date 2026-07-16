package com.trishul.user_service.service;


import com.trishul.user_service.dto.UserRequest;
import com.trishul.user_service.dto.UserResponse;
import com.trishul.user_service.repository.UserRepository;
import com.trishul.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.util.ClassUtils.ifPresent;

@Service
public class UserService {

        private final UserRepository userRepository;

        public UserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Transactional
        public UserResponse createUser(UserRequest request) {

            String normalizedEmail = request.getEmail()
                    .trim()
                    .toLowerCase();

            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new RuntimeException(
                        "User already exists with email: " + normalizedEmail
                );
            }

            User user = new User();

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(normalizedEmail);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);

            return mapToResponse(savedUser);
        }

        @Transactional(readOnly = true)
        public List<UserResponse> getAllUsers() {

            return userRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        @Transactional(readOnly = true)
        public UserResponse getUserById(Long userId) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with id: " + userId
                            )
                    );

            return mapToResponse(user);
        }

        @Transactional
        public UserResponse updateUser(
                Long userId,
                UserRequest request
        ) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with id: " + userId
                            )
                    );

            String normalizedEmail = request.getEmail()
                    .trim()
                    .toLowerCase();

            if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                    normalizedEmail,
                    userId
            )) {
                throw new RuntimeException(
                        "Another user already exists with email: "
                                + normalizedEmail
                );
            }

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(normalizedEmail);

            User updatedUser = userRepository.save(user);

            return mapToResponse(updatedUser);
        }

        @Transactional
        public void deleteUser(Long userId) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with id: " + userId
                            )
                    );

            userRepository.delete(user);
        }

        private UserResponse mapToResponse(User user) {

            return new UserResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getActive()
            );
        }
    }
package com.trishul.user_service;

import com.trishul.user_service.dto.UserRequest;
import com.trishul.user_service.dto.UserResponse;
import com.trishul.user_service.repository.UserRepository;
import com.trishul.user_service.service.UserService;
import com.trishul.user_service.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldCreateUserWithNormalizedEmail() {

        UserRequest request = new UserRequest(
                "Trishul",
                "Reddy",
                "  TRISHUL.REDDY@EXAMPLE.COM  "
        );

        when(userRepository.existsByEmailIgnoreCase(
                "trishul.reddy@example.com"
        )).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Trishul", response.getFirstName());
        assertEquals("Reddy", response.getLastName());
        assertEquals(
                "trishul.reddy@example.com",
                response.getEmail()
        );
        assertTrue(response.getActive());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Trishul", savedUser.getFirstName());
        assertEquals("Reddy", savedUser.getLastName());
        assertEquals(
                "trishul.reddy@example.com",
                savedUser.getEmail()
        );
        assertTrue(savedUser.getActive());
        assertNotNull(savedUser.getCreatedAt());

        verify(userRepository)
                .existsByEmailIgnoreCase(
                        "trishul.reddy@example.com"
                );
    }

    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        UserRequest request = new UserRequest(
                "Trishul",
                "Reddy",
                "TRISHUL@EXAMPLE.COM"
        );

        when(userRepository.existsByEmailIgnoreCase(
                "trishul@example.com"
        )).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(request)
        );

        assertEquals(
                "User already exists with email: trishul@example.com",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {

        User firstUser = createUserEntity(
                1L,
                "Trishul",
                "Reddy",
                "trishul@example.com"
        );

        User secondUser = createUserEntity(
                2L,
                "Max",
                "Mustermann",
                "max@example.com"
        );

        when(userRepository.findAll())
                .thenReturn(List.of(firstUser, secondUser));

        List<UserResponse> responses =
                userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals("Trishul", responses.get(0).getFirstName());
        assertEquals(
                "trishul@example.com",
                responses.get(0).getEmail()
        );

        assertEquals(2L, responses.get(1).getId());
        assertEquals("Max", responses.get(1).getFirstName());
        assertEquals(
                "max@example.com",
                responses.get(1).getEmail()
        );

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUserWhenFound() {

        User user = createUserEntity(
                1L,
                "Trishul",
                "Reddy",
                "trishul@example.com"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Trishul", response.getFirstName());
        assertEquals("Reddy", response.getLastName());
        assertEquals(
                "trishul@example.com",
                response.getEmail()
        );
        assertTrue(response.getActive());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(99L)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository).findById(99L);
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {

        User existingUser = createUserEntity(
                1L,
                "Old",
                "Name",
                "old@example.com"
        );

        UserRequest request = new UserRequest(
                "Trishul",
                "Reddy",
                "  NEW.EMAIL@EXAMPLE.COM  "
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.existsByEmailIgnoreCaseAndIdNot(
                "new.email@example.com",
                1L
        )).thenReturn(false);

        when(userRepository.save(existingUser))
                .thenReturn(existingUser);

        UserResponse response =
                userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Trishul", response.getFirstName());
        assertEquals("Reddy", response.getLastName());
        assertEquals(
                "new.email@example.com",
                response.getEmail()
        );

        verify(userRepository).findById(1L);

        verify(userRepository)
                .existsByEmailIgnoreCaseAndIdNot(
                        "new.email@example.com",
                        1L
                );

        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenAnotherUserHasEmail() {

        User existingUser = createUserEntity(
                1L,
                "Trishul",
                "Reddy",
                "old@example.com"
        );

        UserRequest request = new UserRequest(
                "Trishul",
                "Reddy",
                "EXISTING@EXAMPLE.COM"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.existsByEmailIgnoreCaseAndIdNot(
                "existing@example.com",
                1L
        )).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(1L, request)
        );

        assertEquals(
                "Another user already exists with email: "
                        + "existing@example.com",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowExceptionWhenUserNotFound() {

        UserRequest request = new UserRequest(
                "Trishul",
                "Reddy",
                "trishul@example.com"
        );

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(99L, request)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).existsByEmailIgnoreCaseAndIdNot(
                anyString(),
                anyLong()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteExistingUser() {

        User existingUser = createUserEntity(
                1L,
                "Trishul",
                "Reddy",
                "trishul@example.com"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(existingUser);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.deleteUser(99L)
        );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository).findById(99L);
        verify(userRepository, never()).delete(any(User.class));
    }

    private User createUserEntity(
            Long id,
            String firstName,
            String lastName,
            String email
    ) {

        User user = new User();

        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }
}



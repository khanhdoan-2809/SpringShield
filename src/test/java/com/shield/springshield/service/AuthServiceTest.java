package com.shield.springshield.service;

import com.shield.springshield.entity.User;
import com.shield.springshield.repository.UserRepository;
import com.shield.springshield.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").password("encodedPassword").role("USER").build();
    }

    @Test
    void shouldAuthenticateAndGenerateToken_WhenValidCredentials() {
        // Given
        String rawPassword = "password";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getUsername())).thenReturn("mocked-jwt-token");

        // Act
        String token = authService.authenticateUser("testuser", rawPassword);

        // Assert
        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);

        // Verify
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches(rawPassword, testUser.getPassword());
        verify(jwtUtil, times(1)).generateToken(testUser.getUsername());
    }

    @Test
    void shouldThrowException_WhenInvalidCredentials() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // Act
        Exception exception = assertThrows(RuntimeException.class, () -> authService.authenticateUser("testuser", "wrongPassword"));

        // Assert
        assertEquals("Invalid credentials", exception.getMessage());

        // Verify
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongPassword", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void shouldCreateANewUser_WhenValidParameters() {
        // Given
        String username = "newUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String role = "USER";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Act
        String newUser = authService.registerUser(username, rawPassword, role);

        // Assert
        assertEquals("User registered successfully", newUser);

        // Verify
        verify(userRepository, times(1)).save(any(User.class));
    }
}
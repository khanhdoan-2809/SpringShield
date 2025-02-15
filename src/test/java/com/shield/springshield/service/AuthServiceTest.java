package com.shield.springshield.service;

import com.shield.springshield.model.dto.UserCreateDTO;
import com.shield.springshield.model.entity.Role;
import com.shield.springshield.model.entity.User;
import com.shield.springshield.repository.RoleRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID().toString()).username("testuser").password("encodedPassword").build();
    }

    @Test
    void testAuthenticateAndGenerateToken_WhenValidCredentials() {
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
    void throwException_WhenInvalidCredentials() {
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
    void testRegisterUser_WhenValidParameters() {
        // Given
        UserCreateDTO userCreateDTO = new UserCreateDTO("john_doe", "john@example.com", "password123", "USER");
        Role role = new Role();
        role.setName("USER");
        role.setId(UUID.randomUUID().toString());
        when(roleRepository.findByName(userCreateDTO.getRole())).thenReturn(Optional.of(role));
        when(userRepository.findByEmail(userCreateDTO.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(userCreateDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn("encodedPassword");

        // Act
        String newUser = authService.registerUser(userCreateDTO);

        // Assert
        assertEquals("User registered successfully", newUser);

        // Verify
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_WhenUserAlreadyExists() {
        // Arrange
        UserCreateDTO userCreateDTO = new UserCreateDTO("john@example.com", "john_doe", "password123", "user");
        when(userRepository.findByEmail(userCreateDTO.getEmail())).thenReturn(Optional.of(new User()));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(userCreateDTO);
        });

        // Assert
        assertEquals("User already exists", exception.getMessage());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }
}
package com.shield.springshield.service;

import com.shield.springshield.model.dto.UserCreateDTO;
import com.shield.springshield.model.entity.Role;
import com.shield.springshield.model.entity.User;
import com.shield.springshield.repository.RoleRepository;
import com.shield.springshield.repository.UserRepository;
import com.shield.springshield.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String registerUser(UserCreateDTO userCreateDTO) {
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent() || userRepository.findByUsername(userCreateDTO.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        Role role = roleRepository.findByName(userCreateDTO.getRole()).orElseThrow(() -> new RuntimeException("Role not found"));

        User newUser = User.builder()
                .id(UUID.randomUUID().toString())
                .email(userCreateDTO.getEmail())
                .username(userCreateDTO.getUsername())
                .password(passwordEncoder.encode(userCreateDTO.getPassword()))
                .createdAt(new Date())
                .roles(new HashSet<Role>(Arrays.asList(role)))
                .build();
        userRepository.save(newUser);
        return "User registered successfully";
    }

    public String authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getUsername());
    }
}
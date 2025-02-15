package com.shield.springshield.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class UserCreateDTO {
    private String email;
    private String username;
    private String password;
    private String role;
}

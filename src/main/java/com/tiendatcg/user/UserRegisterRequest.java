package com.tiendatcg.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min=8, max=72)
    private String password;

    public UserRegisterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

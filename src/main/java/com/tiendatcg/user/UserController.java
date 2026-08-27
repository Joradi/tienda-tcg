package com.tiendatcg.user;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponseDto registerUser(@Valid @RequestBody UserRegisterRequest request)
    {
    User user = userService.registerUser(
            request.getEmail(),
            request.getPassword()
    );

    return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getRole()
    );
    }

    @GetMapping("/me")
    public String me(Authentication authentication)
    {
        return authentication.getName();
    }

}

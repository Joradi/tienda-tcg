package com.tiendatcg.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String email, String rawPassword)
    {
        if (userRepository.findByEmail(email).isPresent())
        {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        User user = new User(
                email,
                passwordHash,
                Role.CUSTOMER
        );

        return userRepository.save(user);
    }
}

package com.tiendatcg.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(Authentication authentication)
    {
        Instant now = Instant.now();

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", role)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}

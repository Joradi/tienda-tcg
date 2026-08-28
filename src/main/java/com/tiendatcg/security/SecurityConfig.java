package com.tiendatcg.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.DispatcherType;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.dispatcherTypeMatchers(DispatcherType.ERROR)
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/register")
                                .permitAll().requestMatchers(HttpMethod.POST, "/auth/login")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/cards/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/products/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/cards/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/cards/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/cards/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/products/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/products/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/products/**")
                                .hasRole("ADMIN")
                                .requestMatchers("/cart", "/cart/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/checkout")
                                .permitAll()
                                .requestMatchers("/importations", "/importations/**")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .httpBasic(httpBasic ->
                        httpBasic.disable());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception
    {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter()
    {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return authenticationConverter;
    }
}

package com.tiendatcg.auth;

public class AuthLoginResponse {

    private final String token;

    public AuthLoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

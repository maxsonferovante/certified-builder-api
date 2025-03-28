package com.maal.certifiedbuilderapi.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

@Configuration
public class AuthenticationService {

    private static String apiKey;

    @Value("${api.key}")
    public void setApiKey(String value) {
        apiKey = value;
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String receivedApiKey = request.getHeader("X-API-KEY");

        if (receivedApiKey == null || !receivedApiKey.equals(apiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }

        return new ApiKeyAuthentication(receivedApiKey, AuthorityUtils.NO_AUTHORITIES);
    }
}

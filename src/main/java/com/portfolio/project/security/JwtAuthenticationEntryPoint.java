package com.portfolio.project.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiError apiError = ApiError.builder().status(HttpServletResponse.SC_UNAUTHORIZED).error("Unauthorized")
                .message("Authentication is required. Provide a valid JWT access token.").build();

        response.getWriter().write(new ObjectMapper().writeValueAsString(apiError));
    }

    private static class ApiError {
        private int status;
        private String error;
        private String message;

        private ApiError(int status, String error, String message) {
            this.status = status;
            this.error = error;
            this.message = message;
        }

        public static ApiErrorBuilder builder() {
            return new ApiErrorBuilder();
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public static class ApiErrorBuilder {
            private int status;
            private String error;
            private String message;

            public ApiErrorBuilder status(int status) {
                this.status = status;
                return this;
            }

            public ApiErrorBuilder error(String error) {
                this.error = error;
                return this;
            }

            public ApiErrorBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ApiError build() {
                return new ApiError(status, error, message);
            }
        }
    }
}

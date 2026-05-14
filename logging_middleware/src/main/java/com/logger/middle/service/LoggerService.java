package com.logger.middle.service;

import com.logger.middle.dto.LogRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
public class LoggerService {

    private static final String LOG_API_URL = "http://4.224.186.213/evaluation-service/logs";
    private static final Path FALLBACK_LOG_PATH = Path.of("logs", "logging-middleware-fallback.log");
    private static final Set<String> VALID_STACKS = Set.of("backend", "frontend");
    private static final Set<String> VALID_LEVELS = Set.of("debug", "info", "warn", "error", "fatal");
    private static final Set<String> BACKEND_PACKAGES = Set.of(
            "cache", "controller", "cron_job", "db", "domain",
            "handler", "repository", "route", "service"
    );
    private static final Set<String> FRONTEND_PACKAGES = Set.of(
            "api", "component", "hook", "page", "state", "style"
    );
    private static final Set<String> SHARED_PACKAGES = Set.of("auth", "config", "middleware", "utils");
    private static final Map<String, Set<String>> STACK_PACKAGES = Map.of(
            "backend", BACKEND_PACKAGES,
            "frontend", FRONTEND_PACKAGES
    );

    private final RestTemplate restTemplate;
    private final String authToken;
            
    public LoggerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.authToken = System.getenv("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJNYXBDbGFpbXMiOnsiYXVkIjoiaHR0cDovLzIwLjI0NC41Ni4xNDQvZXZhbHVhdGlvbi1zZXJ2aWNlIiwiZW1haWwiOiJyYWpqYWlzd2FsMjNAbHB1LmluIiwiZXhwIjoxNzc4NzYzNjYxLCJpYXQiOjE3Nzg3NjI3NjEsImlzcyI6IkFmZm9yZCBNZWRpY2FsIFRlY2hub2xvZ2llcyBQcml2YXRlIExpbWl0ZWQiLCJqdGkiOiJjODQyNDA3ZS04MjJiLTQxYWYtODBhZi03NzQ1MGE5NjNiZmMiLCJsb2NhbGUiOiJlbi1JTiIsIm5hbWUiOiJyYWogamFpc3dhbCIsInN1YiI6IjBlOTgwMDU0LTAyNmUtNDk3NC1hOGFlLTRhY2M5M2Q4ODJjNCJ9LCJlbWFpbCI6InJhamphaXN3YWwyM0BscHUuaW4iLCJuYW1lIjoicmFqIGphaXN3YWwiLCJyb2xsTm8iOiIxMjMxNDkzMCIsImFjY2Vzc0NvZGUiOiJUUnZaV3EiLCJjbGllbnRJRCI6IjBlOTgwMDU0LTAyNmUtNDk3NC1hOGFlLTRhY2M5M2Q4ODJjNCIsImNsaWVudFNlY3JldCI6InJtYkNwS0dZbUV6aG5CSkEifQ.PnDbb2i0MgFIdpfJIR_KF2aPkH8ji4KqqyEVsEK3YV0");
    }

    public void log(String stack,
                    String level,
                    String packageName,
                    String message) {
        Log(stack, level, packageName, message);
    }

    public void Log(String stack,
                    String level,
                    String packageName,
                    String message) {

        validate(stack, level, packageName, message);

        LogRequest request =
                new LogRequest(stack, level, packageName, message);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth(requireToken());

        Map<String, String> payload = Map.of(
                "stack", stack,
                "level", level,
                "package", packageName,
                "message", message
        );

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(payload, headers);

        ResponseEntity<String> response;

        try {
            response = restTemplate.postForEntity(
                    LOG_API_URL,
                    entity,
                    String.class
            );
        } catch (RuntimeException exception) {
            writeFallback(request, "Log API request failed: " + exception.getMessage());
            throw exception;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            writeFallback(request, "Log API returned status " + response.getStatusCode().value());
            throw new IllegalStateException("Log API failed with status " + response.getStatusCode().value());
        }
    }

    private String requireToken() {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalStateException("LOG_AUTH_TOKEN environment variable is required");
        }

        return authToken;
    }

    private void validate(String stack, String level, String packageName, String message) {
        if (!VALID_STACKS.contains(stack)) {
            throw new IllegalArgumentException("Invalid stack: " + stack);
        }

        if (!VALID_LEVELS.contains(level)) {
            throw new IllegalArgumentException("Invalid level: " + level);
        }

        boolean validPackage = SHARED_PACKAGES.contains(packageName)
                || STACK_PACKAGES.get(stack).contains(packageName);

        if (!validPackage) {
            throw new IllegalArgumentException("Invalid package for " + stack + " stack: " + packageName);
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Log message must be descriptive and non-empty");
        }
    }

    private void writeFallback(LogRequest request, String reason) {
        String fallbackEntry = "%s stack=%s level=%s package=%s reason=\"%s\" message=\"%s\"%n"
                .formatted(
                        Instant.now(),
                        request.getStack(),
                        request.getLevel(),
                        request.getPackageName(),
                        sanitize(reason),
                        sanitize(request.getMessage())
                );

        try {
            Files.createDirectories(FALLBACK_LOG_PATH.getParent());
            Files.writeString(
                    FALLBACK_LOG_PATH,
                    fallbackEntry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write fallback log entry", exception);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\r", " ").replace("\n", " ");
    }
}

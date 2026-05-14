package com.logger.middle.service;

import com.logger.middle.dto.LogRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoggerService {

    private final RestTemplate restTemplate;

    private final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJNYXBDbGFpbXMiOnsiYXVkIjoiaHR0cDovLzIwLjI0NC41Ni4xNDQvZXZhbHVhdGlvbi1zZXJ2aWNlIiwiZW1haWwiOiJyYWpqYWlzd2FsMjNAbHB1LmluIiwiZXhwIjoxNzc4NzYxNTY4LCJpYXQiOjE3Nzg3NjA2NjgsImlzcyI6IkFmZm9yZCBNZWRpY2FsIFRlY2hub2xvZ2llcyBQcml2YXRlIExpbWl0ZWQiLCJqdGkiOiI1ZGU1ZmYxYi02N2QzLTRjNmUtYjU5MS01MjhhZGYxMjBiN2YiLCJsb2NhbGUiOiJlbi1JTiIsIm5hbWUiOiJyYWogamFpc3dhbCIsInN1YiI6IjBlOTgwMDU0LTAyNmUtNDk3NC1hOGFlLTRhY2M5M2Q4ODJjNCJ9LCJlbWFpbCI6InJhamphaXN3YWwyM0BscHUuaW4iLCJuYW1lIjoicmFqIGphaXN3YWwiLCJyb2xsTm8iOiIxMjMxNDkzMCIsImFjY2Vzc0NvZGUiOiJUUnZaV3EiLCJjbGllbnRJRCI6IjBlOTgwMDU0LTAyNmUtNDk3NC1hOGFlLTRhY2M5M2Q4ODJjNCIsImNsaWVudFNlY3JldCI6InJtYkNwS0dZbUV6aG5CSkEifQ.uj5iy0dzlSvsMNw1K67H06aE4IqSdL8KBkdsgzbpXPY";
    
    public LoggerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void log(String stack,
                    String level,
                    String packageName,
                    String message) {

        String url = "http://4.224.186.213/evaluation-service/logs";

        LogRequest request =
                new LogRequest(stack, level, packageName, message);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth(TOKEN);

        HttpEntity<LogRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        url,
                        entity,
                        String.class
                );

        System.out.println(response.getBody());
    }
}
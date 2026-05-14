package com.vehical.maintence_scheduler.service;

import com.logger.middle.service.LoggerService;
import com.vehical.maintence_scheduler.dto.DepotsResponse;
import com.vehical.maintence_scheduler.dto.VehiclesResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EvaluationClient {

    private final RestClient restClient;
    private final LoggerService loggerService;
    private final String authToken;

    public EvaluationClient(LoggerService loggerService) {
        this.loggerService = loggerService;
        this.authToken = System.getenv("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJNYXBDbGFpbXMiOnsiYXVkIjoiaHR0cDovLzIwLjI0NC41Ni4xNDQvZXZhbHVhdGlvbi1zZXJ2aWNlIiwiZW1haWwiOiJyYWpqYWlzd2FsMjNAbHB1LmluIiwiZXhwIjoxNzc4NzYzNjYxLCJpYXQiOjE3Nzg3NjI3NjEsImlzcyI6IkFmZm9yZCBNZWRpY2FsIFRlY2hub2xvZ2llcyBQcml2YXRlIExpbWl0ZWQiLCJqdGkiOiJjODQyNDA3ZS04MjJiLTQxYWYtODBhZi03NzQ1MGE5NjNiZmMiLCJsb2NhbGUiOiJlbi1JTiIsIm5hbWUiOiJyYWogamFpc3dhbCIsInN1YiI6IjBlOTgwMDU0LTAyNmUtNDk3NC1hOGFlLTRhY2M5M2Q4ODJjNCJ9LCJlbWFpbCI6InJhamphaXN3YWwyM0BscHUuaW4iLCJuYW1lIjoicmFqIGphaXN3YWwiLCJyb2xsTm8iOiIxMjMxNDkzMCIsImFjY2Vzc0NvZGUiOiJUUnZaV3EiLCJjbGllbnRJRCI6IjBlOTgwMDU0LTAyNmUtNDk3NC1hOGFlLTRhY2M5M2Q4ODJjNCIsImNsaWVudFNlY3JldCI6InJtYkNwS0dZbUV6aG5CSkEifQ.PnDbb2i0MgFIdpfJIR_KF2aPkH8ji4KqqyEVsEK3YV0");
        this.restClient = RestClient.builder()
                .baseUrl("http://4.224.186.213/evaluation-service")
                .build();
    }

    public DepotsResponse fetchDepots() {
        loggerService.Log("backend", "info", "service", "Fetching depot mechanic-hour budgets from evaluation-service API");

        return restClient.get()
                .uri("/depots")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .retrieve()
                .body(DepotsResponse.class);
    }

    public VehiclesResponse fetchVehicles() {
        loggerService.Log("backend", "info", "service", "Fetching vehicle maintenance tasks from evaluation-service API");

        return restClient.get()
                .uri("/vehicles")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .retrieve()
                .body(VehiclesResponse.class);
    }

    private String authorizationHeader() {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalStateException("LOG_AUTH_TOKEN environment variable is required for evaluation-service calls");
        }

        return "Bearer " + authToken;
    }
}

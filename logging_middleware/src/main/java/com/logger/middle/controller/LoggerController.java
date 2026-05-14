package com.logger.middle.controller;

import com.logger.middle.service.LoggerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggerController {

    private final LoggerService loggerService;

    public LoggerController(LoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @GetMapping("/test")
    public String test() {

        loggerService.log(
                "backend",
                "info",
                "controller",
                "test endpoint called"
        );

        return "working";
    }
}

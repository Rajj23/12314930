package com.logger.middle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogRequest {

    private String stack;
    private String level;

    @JsonProperty("package")
    private String packageName;

    private String message;
}
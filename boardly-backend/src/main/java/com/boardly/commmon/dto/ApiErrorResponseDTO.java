package com.boardly.commmon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ApiErrorResponseDTO {
    private int statusCode;
    private String error = "error";
    private Instant timestamp;
    private String message;
    private Map<String, String> fieldErrors;

    public ApiErrorResponseDTO(int statusCode, Instant timestamp, String message, String path) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
    }
}

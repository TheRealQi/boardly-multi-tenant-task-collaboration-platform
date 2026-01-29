package com.boardly.commmon.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccessResponseDTO {
    private int statusCode;
    private String status = "success";
    private Instant timestamp;
    private String message;
    private Object body;

    public ApiSuccessResponseDTO(int statusCode, Instant timestamp, String message, Object body) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.body = body;
    }
}

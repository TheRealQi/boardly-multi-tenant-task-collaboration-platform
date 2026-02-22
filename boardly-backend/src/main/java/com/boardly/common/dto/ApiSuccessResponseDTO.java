package com.boardly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiSuccessResponseDTO<T> {
    private int statusCode;
    private String status = "success";
    private Instant timestamp;
    private String message;
    private T data = null;

    public ApiSuccessResponseDTO(int statusCode, Instant timestamp, String message) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
    }

    public ApiSuccessResponseDTO(int statusCode, Instant timestamp, String message, T data) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.data = data;
    }
}

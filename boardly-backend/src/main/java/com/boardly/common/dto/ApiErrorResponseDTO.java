package com.boardly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponseDTO {
    private int statusCode;
    private String status = "error";
    private Instant timestamp;
    private String message;
    private Map<String, List<String>> fieldErrors;

    public ApiErrorResponseDTO(int statusCode, Instant timestamp, String message, String path) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
    }
}

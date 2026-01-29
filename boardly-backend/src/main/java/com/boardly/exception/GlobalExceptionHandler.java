package com.boardly.exception;

import com.boardly.commmon.dto.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidFieldsException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage()));
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("Validation failed for one or more fields.");
        apiErrorResponseDTO.setPath(request.getDescription(false).replace("uri=", ""));
        apiErrorResponseDTO.setFieldErrors(fieldErrors);
        return null;
    }

    @ExceptionHandler(FieldsValidationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleFieldsTakenException(FieldsValidationException ex, WebRequest request) {

        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("Validation failed for one or more fields.");
        apiErrorResponseDTO.setPath(request.getDescription(false).replace("uri=", ""));
        apiErrorResponseDTO.setFieldErrors(ex.getErrors());

        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.BAD_REQUEST);
    }


}

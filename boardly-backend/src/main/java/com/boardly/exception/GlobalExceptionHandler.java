package com.boardly.exception;

import com.boardly.commmon.dto.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
        apiErrorResponseDTO.setFieldErrors(fieldErrors);
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FieldsValidationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleFieldsTakenException(FieldsValidationException ex, WebRequest request) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("Validation failed for one or more fields.");
        apiErrorResponseDTO.setFieldErrors(ex.getErrors());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        ApiErrorResponseDTO error = new ApiErrorResponseDTO();
        error.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        error.setTimestamp(Instant.now());
        error.setMessage("Invalid username or password.");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleForbiddenException(ForbiddenException ex, WebRequest request) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGenericException(Exception ex, WebRequest request) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("An unexpected error occurred.");
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

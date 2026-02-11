package com.boardly.exception;

import com.boardly.commmon.dto.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidFieldsException(
            MethodArgumentNotValidException ex) {

        Map<String, List<String>> fieldErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.groupingBy(
                                FieldError::getField,
                                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                        ));

        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("Validation failed for one or more fields.");
        apiErrorResponseDTO.setFieldErrors(fieldErrors);

        return ResponseEntity.badRequest().body(apiErrorResponseDTO);
    }


    @ExceptionHandler(FieldsValidationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleFieldsTakenException(FieldsValidationException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("Validation failed for one or more fields.");
        Map<String, List<String>> errors = ex.getErrors() != null ? ex.getErrors() : Map.of();
        apiErrorResponseDTO.setFieldErrors(errors);
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
        ApiErrorResponseDTO error = new ApiErrorResponseDTO();
        error.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        error.setTimestamp(Instant.now());
        error.setMessage("Invalid username or password.");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.NOT_FOUND.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleForbiddenException(ForbiddenException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.FORBIDDEN.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleConflictException(ConflictException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.CONFLICT.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleBadRequestException(BadRequestException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        ApiErrorResponseDTO error = new ApiErrorResponseDTO();
        error.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        error.setTimestamp(Instant.now());
        error.setMessage("HTTP method not allowed: " + ex.getMethod());
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ApiErrorResponseDTO error = new ApiErrorResponseDTO();
        error.setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        error.setTimestamp(Instant.now());
        error.setMessage("Unsupported media type: " + ex.getContentType());
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNoHandlerFound(NoHandlerFoundException ex) {
        ApiErrorResponseDTO error = new ApiErrorResponseDTO();
        error.setStatusCode(HttpStatus.NOT_FOUND.value());
        error.setTimestamp(Instant.now());
        error.setMessage("Endpoint not found: " + ex.getRequestURL());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGenericException(Exception ex) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO();
        apiErrorResponseDTO.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiErrorResponseDTO.setTimestamp(Instant.now());
        apiErrorResponseDTO.setMessage("An unexpected error occurred.");
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

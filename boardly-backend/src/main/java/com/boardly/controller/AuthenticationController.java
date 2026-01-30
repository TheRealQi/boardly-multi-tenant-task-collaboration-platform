package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.authentication.LoginRequestDTO;
import com.boardly.commmon.dto.authentication.LoginResponseDTO;
import com.boardly.commmon.dto.authentication.RegisterRequestDTO;
import com.boardly.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("${api.base-path}/${api.version}/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> Register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) {
        authenticationService.register(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponseDTO<LoginResponseDTO>> Login(@RequestBody @Valid LoginRequestDTO loginRequestDTO, HttpServletRequest httpServletRequest) {
        LoginResponseDTO loginResponseDTO = authenticationService.login(loginRequestDTO, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Login successful", loginResponseDTO));
    }
}

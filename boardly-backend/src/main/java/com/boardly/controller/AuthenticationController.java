package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.authentication.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiSuccessResponseDTO<LoginResponseDTO>> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO) {
        LoginResponseDTO loginResponseDTO = authenticationService.refreshToken(refreshTokenRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Tokens refreshed successfully", loginResponseDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> logout(@RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO) {
        authenticationService.logout(refreshTokenRequestDTO);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> forgotPassword(@RequestParam String email) {
        authenticationService.processForgotPassword(email);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Password reset email sent"));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request, @RequestParam String token) {
        authenticationService.resetPassword(request, token);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Password has been reset"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changePassword(@RequestBody @Valid ChangePasswordRequestDTO request, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        authenticationService.changePassword(request, appUserDetails);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Password changed successfully"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmailAddress(token);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Email verified successfully"));
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> sendVerificationEmail(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        authenticationService.sendEmailVerificationEmail(appUserDetails.getUser());
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Verification email sent successfully"));
    }
}

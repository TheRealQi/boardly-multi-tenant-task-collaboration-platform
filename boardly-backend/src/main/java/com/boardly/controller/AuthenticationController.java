package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.authentication.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("${api.base-path}/${api.version}/auth")
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
            description = "Register endpoint",
            summary = "Register a new user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> Register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) {
        authenticationService.register(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Registration successful"));
    }
    @Operation(
            description = "Login endpoint",
            summary = "Login a user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponseDTO<LoginResponseDTO>> Login(@RequestBody @Valid LoginRequestDTO loginRequestDTO, HttpServletRequest httpServletRequest) {
        LoginResponseDTO loginResponseDTO = authenticationService.login(loginRequestDTO, httpServletRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Login successful", loginResponseDTO));
    }

    @Operation(
            description = "Refresh token endpoint",
            summary = "Refresh a user's token",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiSuccessResponseDTO<LoginResponseDTO>> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO) {
        LoginResponseDTO loginResponseDTO = authenticationService.refreshToken(refreshTokenRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Tokens refreshed successfully", loginResponseDTO));
    }

    @Operation(
            description = "Logout endpoint",
            summary = "Logout a user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> logout(@RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO) {
        authenticationService.logout(refreshTokenRequestDTO);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Logout successful"));
    }

    @Operation(
            description = "Forgot password endpoint",
            summary = "Send a password reset email",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> forgotPassword(@RequestParam String email) {
        authenticationService.processForgotPassword(email);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Password reset email sent"));
    }

    @Operation(
            description = "Reset password endpoint",
            summary = "Reset a user's password",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/reset-password")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request, @RequestParam String token) {
        authenticationService.resetPassword(request, token);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Password has been reset"));
    }

    @Operation(
            description = "Change password endpoint",
            summary = "Change a user's password",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PutMapping("/change-password")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> changePassword(@RequestBody @Valid ChangePasswordRequestDTO request, @AuthenticationPrincipal AppUserDetails appUserDetails) {
        authenticationService.changePassword(request, appUserDetails);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Password changed successfully"));
    }

    @Operation(
            description = "Verify email endpoint",
            summary = "Verify a user's email",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/verify-email")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmailAddress(token);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Email verified successfully"));
    }

    @Operation(
            description = "Send verification email endpoint",
            summary = "Send a verification email",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )
    @PostMapping("/send-verification-email")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> sendVerificationEmail(@AuthenticationPrincipal AppUserDetails appUserDetails) {
        authenticationService.sendEmailVerificationEmail(appUserDetails.getUser());
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Verification email sent successfully"));
    }
}

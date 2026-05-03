package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.UserDTO;
import com.boardly.common.dto.UpdateUserProfileRequestDTO;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.FileStorageService;
import com.boardly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}/${api.version}/users")
@Tag(name = "User")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(
            description = "Get current user profile endpoint",
            summary = "Get the profile of the currently authenticated user",
            responses = {
                    @ApiResponse(description = "Success",                responseCode = "200"),
                    @ApiResponse(description = "Unauthorized",           responseCode = "401"),
                    @ApiResponse(description = "Forbidden",              responseCode = "403")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiSuccessResponseDTO<UserDTO>> getCurrentUserProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        UserDTO userDTO = userService.getUserProfile(userDetails.getUserId());
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User profile retrieved successfully", userDTO));
    }

    @Operation(
            description = "Get user profile by ID endpoint",
            summary = "Get the public profile of a user by their ID",
            responses = {
                    @ApiResponse(description = "Success",       responseCode = "200"),
                    @ApiResponse(description = "User not found", responseCode = "404")
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ApiSuccessResponseDTO<UserDTO>> getUserProfile(@PathVariable UUID userId) {
        UserDTO userDTO = userService.getUserProfile(userId);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User profile retrieved successfully", userDTO));
    }

    @Operation(
            description = "Update current user profile endpoint",
            summary = "Partially update the profile of the currently authenticated user",
            responses = {
                    @ApiResponse(description = "Success",    responseCode = "200"),
                    @ApiResponse(description = "Unauthorized", responseCode = "401"),
                    @ApiResponse(description = "Forbidden",  responseCode = "403")
            }
    )
    @PatchMapping("/me")
    public ResponseEntity<ApiSuccessResponseDTO<UserDTO>> updateUserProfile(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody UpdateUserProfileRequestDTO request) {
        UserDTO updatedUser = userService.updateUserProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User profile updated successfully", updatedUser));
    }

    @Operation(
            description = "Upload profile picture endpoint",
            summary = "Upload a new profile picture for the currently authenticated user",
            responses = {
                    @ApiResponse(description = "Success",    responseCode = "200"),
                    @ApiResponse(description = "Unauthorized", responseCode = "401"),
                    @ApiResponse(description = "Forbidden",  responseCode = "403"),
                    @ApiResponse(description = "Invalid file", responseCode = "400")
            }
    )
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiSuccessResponseDTO<String>> uploadProfilePicture(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        fileStorageService.validateFile(file);
        String fileUri = userService.uploadProfilePicture(userDetails.getUserId(), file);
        return ResponseEntity.ok(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Profile picture uploaded successfully", fileUri));
    }
}
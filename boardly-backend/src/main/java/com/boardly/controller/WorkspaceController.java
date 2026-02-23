package com.boardly.controller;

import com.boardly.common.dto.ApiSuccessResponseDTO;
import com.boardly.common.dto.workspace.*;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-path}/${api.version}/workspace")
@Tag(name = "Workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Operation(
            description = "Create workspace endpoint",
            summary = "Create a new workspace",
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
    @PostMapping("/")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> createWorkspace(@Valid @RequestBody WorkspaceCreationDTO workspaceCreationDTO, @AuthenticationPrincipal AppUserDetails userPrincipal) {

        WorkspaceDetailsDTO workspace = workspaceService.createWorkspace(workspaceCreationDTO, userPrincipal);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Workspace created successfully", workspace));
    }

    @Operation(
            description = "Get workspace endpoint",
            summary = "Get a workspace by ID",
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
    @GetMapping("/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> getWorkspace(@PathVariable UUID workspaceId) {
        WorkspaceDetailsDTO workspaceDTO = workspaceService.getWorkspaceDetails(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace retrieved successfully", workspaceDTO));
    }

    @Operation(
            description = "Get all workspaces endpoint",
            summary = "Get all workspaces for a user",
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
    @GetMapping("/")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceDTO>>> getAllWorkspaces(@AuthenticationPrincipal AppUserDetails userPrincipal) {
        List<WorkspaceDTO> workspaces = workspaceService.getAllWorkspacesForUser(userPrincipal);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User's Workspaces retrieved successfully", workspaces));
    }

    @Operation(
            description = "Delete workspace endpoint",
            summary = "Delete a workspace by ID",
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
    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.canDeleteWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteWorkspace(@PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace deleted successfully", null));
    }

    @Operation(
            description = "Edit workspace endpoint",
            summary = "Edit a workspace by ID",
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
    @PutMapping("/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.canEditWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> editWorkspace(@PathVariable UUID workspaceId, @Valid @RequestBody EditWorkspaceRequestDTO workspaceEditDTO) {
        workspaceService.editWorkspace(workspaceId, workspaceEditDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace updated successfully"));
    }

    @Operation(
            description = "Edit workspace settings endpoint",
            summary = "Edit a workspace's settings",
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
    @PutMapping("/{workspaceId}/settings")
    @PreAuthorize("@authorizationSecurityService.canEditWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> editWorkspaceSettings(@PathVariable UUID workspaceId, @Valid @RequestBody EditWorkspaceSettingsRequestDTO editWorkspaceSettingsRequestDTO) {
        workspaceService.editWorkspaceSettings(workspaceId, editWorkspaceSettingsRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace updated successfully"));
    }
}

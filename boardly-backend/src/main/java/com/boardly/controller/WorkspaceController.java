package com.boardly.controller;

import com.boardly.commmon.dto.ApiSuccessResponseDTO;
import com.boardly.commmon.dto.workspace.EditWorkspaceRequestDTO;
import com.boardly.commmon.dto.workspace.WorkspaceCreationDTO;
import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.dto.workspace.WorkspaceDetailsDTO;
import com.boardly.data.model.workspace.Workspace;
import com.boardly.security.model.AppUserDetails;
import com.boardly.service.WorkspaceService;
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
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping("/")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> createWorkspace(@Valid @RequestBody WorkspaceCreationDTO workspaceCreationDTO, @AuthenticationPrincipal AppUserDetails userPrincipal) {

        WorkspaceDetailsDTO workspace = workspaceService.createWorkspace(workspaceCreationDTO, userPrincipal);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiSuccessResponseDTO<>(HttpStatus.CREATED.value(), Instant.now(), "Workspace created successfully", workspace));
    }

    @GetMapping("/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.isWorkspaceMember(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> getWorkspace(@PathVariable UUID workspaceId, @AuthenticationPrincipal AppUserDetails userPrincipal) {
        WorkspaceDetailsDTO workspaceDTO = workspaceService.getWorkspace(workspaceId, userPrincipal);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace retrieved successfully", workspaceDTO));
    }

    @GetMapping("/")
    public ResponseEntity<ApiSuccessResponseDTO<List<WorkspaceDTO>>> getAllWorkspaces(@AuthenticationPrincipal AppUserDetails userPrincipal) {
        List<WorkspaceDTO> workspaces = workspaceService.getAllWorkspacesForUser(userPrincipal);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "User's Workspaces retrieved successfully", workspaces));
    }

    @DeleteMapping("/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.canDeleteWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<Void>> deleteWorkspace(@PathVariable UUID workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace deleted successfully", null));
    }

    @PutMapping("/{workspaceId}")
    @PreAuthorize("@authorizationSecurityService.canEditWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> editWorkspace(@PathVariable UUID workspaceId, @Valid @RequestBody EditWorkspaceRequestDTO workspaceEditDTO) {
        WorkspaceDetailsDTO updatedWorkspace = workspaceService.editWorkspace(workspaceId, workspaceEditDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace updated successfully", updatedWorkspace));
    }

    @PutMapping("/{workspaceId}/settings")
    @PreAuthorize("@authorizationSecurityService.canEditWorkspace(#workspaceId)")
    public ResponseEntity<ApiSuccessResponseDTO<WorkspaceDetailsDTO>> editWorkspaceSettings(@PathVariable UUID workspaceId, @Valid @RequestBody EditWorkspaceRequestDTO workspaceEditDTO) {
        WorkspaceDetailsDTO updatedWorkspace = workspaceService.editWorkspace(workspaceId, workspaceEditDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiSuccessResponseDTO<>(HttpStatus.OK.value(), Instant.now(), "Workspace updated successfully", updatedWorkspace));
    }
}

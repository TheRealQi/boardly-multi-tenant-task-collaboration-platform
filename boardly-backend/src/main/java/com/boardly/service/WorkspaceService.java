package com.boardly.service;

import com.boardly.commmon.dto.workspace.EditWorkspaceRequestDTO;
import com.boardly.commmon.dto.workspace.WorkspaceCreationDTO;
import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.dto.workspace.WorkspaceSettingsDTO;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.Workspace;
import com.boardly.data.model.WorkspaceMember;
import com.boardly.data.model.WorkspaceSettings;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import com.boardly.exception.ForbiddenException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Transactional
    public Workspace createWorkspace(WorkspaceCreationDTO workspaceCreationDTO, AppUserDetails userPrincipal) {
        String title = workspaceCreationDTO.getTitle();
        String description = workspaceCreationDTO.getDescription();

        Workspace workspace = new Workspace();
        workspace.setTitle(title);
        workspace.setDescription(description);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = new WorkspaceMember();
        workspaceMember.setWorkspace(savedWorkspace);
        workspaceMember.setUser(userPrincipal.getUser());
        workspaceMember.setRole(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(workspaceMember);

        return savedWorkspace;
    }

    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found");
        }
        workspaceRepository.deleteById(workspaceId);
    }

    public WorkspaceDTO getWorkspace(UUID workspaceId, AppUserDetails userPrincipal) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        WorkspaceRole userRole = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userPrincipal.getUserId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of the workspace"));
        WorkspaceDTO workspaceDTO = new WorkspaceDTO();
        workspaceDTO.setWorkspaceId(workspace.getId());
        workspaceDTO.setTitle(workspace.getTitle());
        workspaceDTO.setDescription(workspace.getDescription());
        workspaceDTO.setRole(userRole);
        return workspaceDTO;
    }

    public List<WorkspaceDTO> getAllWorkspacesForUser(AppUserDetails userPrincipal) {
        UUID userId = userPrincipal.getUserId();
        List<WorkspaceMember> workspaceMemberships = workspaceMemberRepository.findAllByUserId(userId);
        List<WorkspaceDTO> workspaceDTOList = workspaceMemberships.stream()
                .map(m -> {
                    Workspace workspace = m.getWorkspace();
                    WorkspaceDTO dto = new WorkspaceDTO();
                    dto.setWorkspaceId(workspace.getId());
                    dto.setTitle(workspace.getTitle());
                    dto.setDescription(workspace.getDescription());
                    dto.setRole(m.getRole());
                    return dto;
                }).collect(Collectors.toList());
        return workspaceDTOList;
    }

    @Transactional
    public WorkspaceSettingsDTO editWorkspace(UUID workspaceId, EditWorkspaceRequestDTO editWorkspaceRequestDTO) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        workspace.setTitle(editWorkspaceRequestDTO.getTitle());
        workspace.setDescription(editWorkspaceRequestDTO.getDescription());
        WorkspaceSettings settings = workspace.getSettings();
        settings.setPrivateBoardCreation(editWorkspaceRequestDTO.getPrivateBoardCreationSetting());
        settings.setWorkspaceVisibleBoardCreation(editWorkspaceRequestDTO.getWorkspaceBoardCreationSetting());
        workspace.setSettings(settings);
        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        WorkspaceSettingsDTO workspaceSettingsDTO = new WorkspaceSettingsDTO();
        workspaceSettingsDTO.setWorkspaceId(updatedWorkspace.getId());
        workspaceSettingsDTO.setTitle(updatedWorkspace.getTitle());
        workspaceSettingsDTO.setDescription(updatedWorkspace.getDescription());
        workspaceSettingsDTO.setPrivateBoardCreationSetting(updatedWorkspace.getSettings().getPrivateBoardCreation());
        workspaceSettingsDTO.setWorkspaceBoardCreationSetting(updatedWorkspace.getSettings().getWorkspaceVisibleBoardCreation());
        return workspaceSettingsDTO;
    }
}

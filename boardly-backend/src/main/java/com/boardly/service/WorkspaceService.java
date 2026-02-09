package com.boardly.service;

import com.boardly.commmon.dto.workspace.*;
import com.boardly.commmon.enums.BoardCreationSetting;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.mapper.WorkspaceMapper;
import com.boardly.data.model.sql.workspace.Workspace;
import com.boardly.data.model.sql.workspace.WorkspaceBoardCreationSetting;
import com.boardly.data.model.sql.workspace.WorkspaceMember;
import com.boardly.data.repository.BoardRepository;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceMapper workspaceMapper;
    private final BoardRepository boardRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository, WorkspaceMapper workspaceMapper, BoardRepository boardRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.workspaceMapper = workspaceMapper;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public WorkspaceDetailsDTO createWorkspace(WorkspaceCreationDTO workspaceCreationDTO, AppUserDetails userPrincipal) {
        Workspace workspace = workspaceMapper.toEntity(workspaceCreationDTO);
        WorkspaceBoardCreationSetting workspaceBoardCreationSetting = new WorkspaceBoardCreationSetting();
        workspaceBoardCreationSetting.setPrivateBoardCreation(BoardCreationSetting.ANY_MEMBER);
        workspaceBoardCreationSetting.setWorkspaceVisibleBoardCreation(BoardCreationSetting.ANY_MEMBER);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = new WorkspaceMember();
        workspaceMember.setWorkspace(savedWorkspace);
        workspaceMember.setUser(userPrincipal.getUser());
        workspaceMember.setRole(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(workspaceMember);

        return workspaceMapper.toDetailsDto(workspace);
    }

    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        workspaceRepository.delete(workspace);

        boardRepository.deleteAllByWorkspace(workspace);
    }

    public WorkspaceDetailsDTO getWorkspaceDetails(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        return workspaceMapper.toDetailsDto(workspace);
    }


    public List<WorkspaceDTO> getAllWorkspacesForUser(AppUserDetails userPrincipal) {
        return workspaceRepository.findAllWorkspaceDTOsByUser(userPrincipal.getUser());
    }

    @Transactional
    public void editWorkspace(UUID workspaceId, EditWorkspaceRequestDTO editWorkspaceRequestDTO) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        workspace.setTitle(editWorkspaceRequestDTO.getTitle());
        workspace.setDescription(editWorkspaceRequestDTO.getDescription());
        WorkspaceBoardCreationSetting settings = workspace.getBoardCreationSettings();
        workspace.setBoardCreationSettings(settings);
        workspaceRepository.save(workspace);
    }

    @Transactional
    public void editWorkspaceSettings(UUID workspaceId, EditWorkspaceSettingsRequestDTO editWorkspaceSettingsRequestDTO) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        WorkspaceBoardCreationSetting boardCreationSettings = new WorkspaceBoardCreationSetting();
        boardCreationSettings.setPrivateBoardCreation(editWorkspaceSettingsRequestDTO.getPrivateBoardCreationSetting());
        boardCreationSettings.setWorkspaceVisibleBoardCreation(editWorkspaceSettingsRequestDTO.getWorkspaceBoardCreationSetting());
        workspace.setBoardCreationSettings(boardCreationSettings);

        workspaceRepository.save(workspace);
    }

}

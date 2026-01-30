package com.boardly.service;

import com.boardly.commmon.dto.workspace.WorkspaceMemberDTO;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkspaceMembershipService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceMembershipService(WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public void leaveWorkspace(UUID workspaceId) {
    }

    public void removeMemberFromWorkspace(UUID workspaceId, UUID memberId) {
    }

    public void inviteMemberToWorkspace(UUID workspaceId, String email) {
    }


}

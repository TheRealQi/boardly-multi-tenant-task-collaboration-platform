package com.boardly.service;

import com.boardly.commmon.dto.workspace.ChangeRoleDTO;
import com.boardly.commmon.dto.workspace.WorkspaceInviteDTO;
import com.boardly.commmon.enums.InviteStatus;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.authentication.User;
import com.boardly.data.model.workspace.Workspace;
import com.boardly.data.model.workspace.WorkspaceInvite;
import com.boardly.data.model.workspace.WorkspaceMember;
import com.boardly.data.repository.UserRepository;
import com.boardly.data.repository.WorkspaceInviteRepository;
import com.boardly.data.repository.WorkspaceMemberRepository;
import com.boardly.data.repository.WorkspaceRepository;
import com.boardly.exception.BadRequestException;
import com.boardly.exception.ForbiddenException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceMembershipService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceInviteRepository workspaceInviteRepository;

    public WorkspaceMembershipService(WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository, UserRepository userRepository, WorkspaceInviteRepository workspaceInviteRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.workspaceInviteRepository = workspaceInviteRepository;
    }

    @Transactional
    public void leaveWorkspace(UUID workspaceId, AppUserDetails appUserDetails) {
        UUID userId = appUserDetails.getUserId();
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Workspace owners cannot leave the workspace. Please transfer ownership or delete the workspace.");
        }
        workspaceMemberRepository.delete(member);
    }

    @Transactional
    public WorkspaceInviteDTO inviteMemberToWorkspace(UUID workspaceId, String email, AppUserDetails appUserDetails) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User userToInvite = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userToInvite.getId())) {
            throw new BadRequestException("User is already a member of this workspace");
        }

        if (workspaceInviteRepository.existsByWorkspaceIdAndUserIdAndStatus(workspaceId, userToInvite.getId(), InviteStatus.PENDING)) {
            throw new BadRequestException("User already has a pending invitation");
        }


        WorkspaceInvite invite = new WorkspaceInvite();
        invite.setWorkspace(workspace);
        invite.setUser(userToInvite);
        invite.setInvitedBy(appUserDetails.getUserId());
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invite.setStatus(InviteStatus.PENDING);

        workspaceInviteRepository.save(invite);

        return new WorkspaceInviteDTO(invite.getId(), invite.getWorkspaceId(), workspace.getTitle(), invite.getInvitedBy(), invite.getStatus(), invite.getExpiresAt());
    }

    @Transactional
    public void removeMemberFromWorkspace(UUID workspaceId, UUID memberId, AppUserDetails appUserDetails) {
        if (memberId.equals(appUserDetails.getUserId())) {
            throw new BadRequestException("Use leave workspace endpoint to leave the workspace.");
        }
        WorkspaceMember memberToRemove = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (memberToRemove.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Cannot remove the workspace owner.");
        }
        workspaceMemberRepository.delete(memberToRemove);
    }

    @Transactional
    public void acceptInvitationToWorkspace(UUID invitationId, AppUserDetails appUserDetails) {
        WorkspaceInvite invite = workspaceInviteRepository.findByIdAndUserId(invitationId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));


        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            workspaceInviteRepository.save(invite);
            throw new BadRequestException("Invitation has expired");
        }

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(invite.getWorkspace().getId(), appUserDetails.getUserId())) {
            throw new BadRequestException("You are already a member of this workspace");
        }

        WorkspaceMember newMember = new WorkspaceMember();
        newMember.setWorkspace(invite.getWorkspace());
        newMember.setUser(invite.getUser());
        newMember.setRole(WorkspaceRole.MEMBER);
        workspaceMemberRepository.save(newMember);

        invite.setStatus(InviteStatus.ACCEPTED);
        workspaceInviteRepository.save(invite);
    }

    @Transactional
    public void declineInvitationToWorkspace(UUID invitationId, AppUserDetails appUserDetails) {
        WorkspaceInvite invite = workspaceInviteRepository.findByIdAndUserId(invitationId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));


        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }

        invite.setStatus(InviteStatus.DECLINED);
        workspaceInviteRepository.save(invite);
    }

    @Transactional
    public void changeWorkspaceMemberRole(UUID workspaceId, UUID memberId, ChangeRoleDTO changeRoleDTO) {
        WorkspaceRole newRole = changeRoleDTO.getRole();
        if (newRole == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot assign Owner role. Use transfer ownership.");
        }

        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Cannot change the Owner's role.");
        }

        targetMember.setRole(newRole);
        workspaceMemberRepository.save(targetMember);
    }

    @Transactional
    public void transferWorkspaceOwnership(UUID workspaceId, UUID newOwnerId, AppUserDetails currentUser) {
        WorkspaceMember currentOwner = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUser.getUserId())
                .orElseThrow(() -> new ForbiddenException("You are not a member of this workspace"));

        if (currentOwner.getRole() != WorkspaceRole.OWNER) {
            throw new ForbiddenException("Only the Owner can transfer ownership.");
        }

        WorkspaceMember newOwner = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this workspace"));

        currentOwner.setRole(WorkspaceRole.ADMIN);
        newOwner.setRole(WorkspaceRole.OWNER);

        workspaceMemberRepository.save(currentOwner);
        workspaceMemberRepository.save(newOwner);
    }

    public List<WorkspaceInviteDTO> getUserPendingWorkspaceInvites(AppUserDetails currentUser) {
        return workspaceInviteRepository.findAllByUserIdAndStatus(currentUser.getUserId(), InviteStatus.PENDING).stream()
                .map(invite -> new WorkspaceInviteDTO(
                        invite.getId(),
                        invite.getWorkspace().getId(),
                        invite.getWorkspace().getTitle(),
                        invite.getInvitedBy(),
                        invite.getStatus(),
                        invite.getExpiresAt()
                )).collect(Collectors.toList());
    }

    public List<WorkspaceInviteDTO> getWorkspacePendingInvites(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        return workspaceInviteRepository.findAllByWorkspaceIdAndStatus(workspaceId, InviteStatus.PENDING).stream()
                .map(invite -> new WorkspaceInviteDTO(
                        invite.getId(),
                        invite.getWorkspace().getId(),
                        workspace.getTitle(),
                        invite.getInvitedBy(),
                        invite.getStatus(),
                        invite.getExpiresAt()
                )).collect(Collectors.toList());
    }
}

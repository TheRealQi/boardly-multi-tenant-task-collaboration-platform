package com.boardly.service;

import com.boardly.common.dto.UserDTO;
import com.boardly.common.dto.workspace.WorkspaceChangeRoleDTO;
import com.boardly.common.dto.workspace.WorkspaceDTO;
import com.boardly.common.dto.workspace.WorkspaceInviteDTO;
import com.boardly.common.dto.workspace.WorkspaceMemberDTO;
import com.boardly.common.enums.InviteStatus;
import com.boardly.common.enums.WorkspaceRole;
import com.boardly.data.mapper.UserMapper;
import com.boardly.data.mapper.WorkspaceMapper;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.workspace.Workspace;
import com.boardly.data.model.sql.workspace.WorkspaceInvite;
import com.boardly.data.model.sql.workspace.WorkspaceMember;
import com.boardly.data.repository.*;
import com.boardly.exception.BadRequestException;
import com.boardly.exception.ConflictException;
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
    private final UserMapper userMapper;
    private final WorkspaceMapper workspaceMapper;
    private final BoardMemberRepository boardMemberRepository;
    private final NotificationService notificationService;

    public WorkspaceMembershipService(WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository, UserRepository userRepository, WorkspaceInviteRepository workspaceInviteRepository, UserMapper userMapper, WorkspaceMapper workspaceMapper, BoardMemberRepository boardMemberRepository, NotificationService notificationService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.workspaceInviteRepository = workspaceInviteRepository;
        this.userMapper = userMapper;
        this.workspaceMapper = workspaceMapper;
        this.boardMemberRepository = boardMemberRepository;
        this.notificationService = notificationService;
    }


    public List<WorkspaceMemberDTO> getWorkspaceMembers(UUID workspaceId) {
        return workspaceMemberRepository.findAllByWorkspace_Id(workspaceId).stream()
                .map(member -> {
                    UserDTO userDTO = new UserDTO(
                            member.getUser().getId(),
                            member.getUser().getUsername(),
                            member.getUser().getFirstName(),
                            member.getUser().getLastName(),
                            member.getUser().getProfilePictureUri()
                    );
                    return new WorkspaceMemberDTO(userDTO, member.getRole(), member.getCreatedAt());
                }).collect(Collectors.toList());
    }

    @Transactional
    public void leaveWorkspace(UUID workspaceId, AppUserDetails appUserDetails) {
        UUID userId = appUserDetails.getUserId();
        WorkspaceMember member = workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this workspace"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Workspace owners cannot leave the workspace. Please transfer ownership or delete the workspace.");
        }
        workspaceMemberRepository.delete(member);
        notificationService.sendToTopic("/topic/workspace/" + workspaceId, "User " + appUserDetails.getUsername() + " left the workspace");
    }

    @Transactional
    public WorkspaceInviteDTO inviteMemberToWorkspace(UUID workspaceId, String email, AppUserDetails appUserDetails) {
        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User userToInvite = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));

        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(workspaceId, userToInvite.getId())) {
            throw new ConflictException("User is already a member of this workspace");
        }

        if (workspaceInviteRepository.existsByWorkspace_IdAndInvitee_IdAndStatus(workspaceId, userToInvite.getId(), InviteStatus.PENDING)) {
            throw new ConflictException("User already has a pending invitation");
        }

        WorkspaceInvite invite = new WorkspaceInvite();
        invite.setWorkspace(workspace);
        invite.setInvitee(userToInvite);
        invite.setInviter(appUserDetails.getUser());
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invite.setStatus(InviteStatus.PENDING);

        workspaceInviteRepository.save(invite);

        UserDTO inviteeDTO = userMapper.toDTO(userToInvite);
        UserDTO inviterDTO = userMapper.toDTO(appUserDetails.getUser());

        WorkspaceInviteDTO inviteDTO = new WorkspaceInviteDTO(invite.getId(), workspaceMapper.toDto(workspace, null), inviteeDTO, inviterDTO, invite.getStatus(), invite.getExpiresAt());
        notificationService.sendToUser(userToInvite.getId(), "/queue/invites", inviteDTO);
        return inviteDTO;
    }

    @Transactional
    public void removeMemberFromWorkspace(UUID workspaceId, UUID memberId, AppUserDetails appUserDetails) {
        if (memberId.equals(appUserDetails.getUserId())) {
            throw new BadRequestException("Cannot remove yourself. Use leave workspace instead.");
        }
        WorkspaceMember memberToRemove = workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (memberToRemove.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Cannot remove the workspace owner.");
        }
        workspaceMemberRepository.delete(memberToRemove);
        boardMemberRepository.deleteAllByWorkspaceIdAndUserId(workspaceId, memberId);
        notificationService.sendToUser(memberId, "/queue/workspace", "You have been removed from the workspace");
        notificationService.sendToTopic("/topic/workspace/" + workspaceId, "User " + memberToRemove.getUser().getUsername() + " has been removed from the workspace");
    }

    @Transactional
    public WorkspaceDTO acceptInvitationToWorkspace(UUID invitationId, AppUserDetails appUserDetails) {
        WorkspaceInvite invite = workspaceInviteRepository.findByIdAndInvitee_Id(invitationId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));


        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            workspaceInviteRepository.save(invite);
            throw new BadRequestException("Invitation has expired");
        }

        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(invite.getWorkspace().getId(), appUserDetails.getUserId())) {
            throw new ConflictException("You are already a member of this workspace");
        }

        WorkspaceMember newMember = new WorkspaceMember();
        newMember.setWorkspace(invite.getWorkspace());
        newMember.setUser(invite.getInvitee());
        newMember.setRole(WorkspaceRole.MEMBER);
        workspaceMemberRepository.save(newMember);

        invite.setStatus(InviteStatus.ACCEPTED);
        workspaceInviteRepository.save(invite);
        notificationService.sendToTopic("/topic/workspace/" + invite.getWorkspace().getId(), "User " + appUserDetails.getUsername() + " joined the workspace");
        return new WorkspaceDTO(
                invite.getWorkspace().getId(),
                invite.getWorkspace().getTitle(),
                invite.getWorkspace().getDescription(),
                newMember.getRole()
        );
    }

    @Transactional
    public void declineInvitationToWorkspace(UUID invitationId, AppUserDetails appUserDetails) {
        WorkspaceInvite invite = workspaceInviteRepository.findByIdAndInvitee_Id(invitationId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));


        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new ConflictException("Invitation is not pending");
        }

        invite.setStatus(InviteStatus.DECLINED);
        workspaceInviteRepository.save(invite);
        notificationService.sendToUser(invite.getInviter().getId(), "/queue/invites", "Your invitation to " + appUserDetails.getUsername() + " has been declined");
    }

    @Transactional
    public void cancelWorkspaceInvitation(UUID inviteId) {
        WorkspaceInvite invite = workspaceInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }
        invite.setStatus(InviteStatus.CANCELLED);
        workspaceInviteRepository.save(invite);
    }

    @Transactional
    public WorkspaceMemberDTO changeWorkspaceMemberRole(UUID workspaceId, UUID memberId, WorkspaceChangeRoleDTO workspaceChangeRoleDTO) {
        WorkspaceRole newRole = workspaceChangeRoleDTO.getRole();
        if (newRole == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot assign Owner role. Use transfer ownership.");
        }

        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenException("Cannot change the Owner's role.");
        }

        targetMember.setRole(newRole);
        workspaceMemberRepository.save(targetMember);

        UserDTO userDTO = userMapper.toDTO(targetMember.getUser());
        notificationService.sendToUser(memberId, "/queue/workspace", "Your role in the workspace has been changed to " + newRole);
        notificationService.sendToTopic("/topic/workspace/" + workspaceId, "User " + targetMember.getUser().getUsername() + "'s role has been changed to " + newRole);
        return new WorkspaceMemberDTO(userDTO, targetMember.getRole(), targetMember.getCreatedAt());
    }

    @Transactional
    public WorkspaceMemberDTO transferWorkspaceOwnership(UUID workspaceId, UUID newOwnerId, AppUserDetails currentUser) {
        WorkspaceMember currentOwner = workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, currentUser.getUserId())
                .orElseThrow(() -> new ForbiddenException("Workspace not found or you are not a member of this workspace"));

        if (currentOwner.getRole() != WorkspaceRole.OWNER) {
            throw new ForbiddenException("Only the Owner can transfer ownership.");
        }

        WorkspaceMember newOwner = workspaceMemberRepository.findByWorkspace_IdAndUser_Id(workspaceId, newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this workspace"));

        currentOwner.setRole(WorkspaceRole.ADMIN);
        newOwner.setRole(WorkspaceRole.OWNER);

        workspaceMemberRepository.save(currentOwner);
        workspaceMemberRepository.save(newOwner);

        UserDTO userDTO = userMapper.toDTO(newOwner.getUser());
        notificationService.sendToUser(newOwnerId, "/queue/workspace", "You are now the owner of the workspace");
        notificationService.sendToTopic("/topic/workspace/" + workspaceId, "Ownership of the workspace has been transferred to " + newOwner.getUser().getUsername());
        return new WorkspaceMemberDTO(userDTO, newOwner.getRole(), newOwner.getCreatedAt());
    }

    public List<WorkspaceInviteDTO> getUserPendingWorkspaceInvites(AppUserDetails currentUser) {
        return workspaceInviteRepository.findAllByInviteeAndStatus(currentUser.getUser(), InviteStatus.PENDING).stream()
                .map(invite -> new WorkspaceInviteDTO(
                        invite.getId(),
                        new WorkspaceDTO(
                                invite.getWorkspace().getId(),
                                invite.getWorkspace().getTitle(),
                                invite.getWorkspace().getDescription(),
                                null
                        ),
                        new UserDTO(
                                invite.getInviter().getId(),
                                invite.getInviter().getUsername(),
                                invite.getInviter().getFirstName(),
                                invite.getInviter().getLastName(),
                                invite.getInviter().getProfilePictureUri()
                        ),
                        invite.getStatus(),
                        invite.getExpiresAt()
                )).collect(Collectors.toList());
    }

    public List<WorkspaceInviteDTO> getWorkspacePendingInvites(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        return workspaceInviteRepository.findAllByWorkspace_IdAndStatus(workspaceId, InviteStatus.PENDING).stream()
                .map(invite -> new WorkspaceInviteDTO(
                        invite.getId(),
                        new UserDTO(
                                invite.getInvitee().getId(),
                                invite.getInvitee().getUsername(),
                                invite.getInvitee().getFirstName(),
                                invite.getInvitee().getLastName(),
                                invite.getInvitee().getProfilePictureUri()
                        ),
                        new UserDTO(
                                invite.getInviter().getId(),
                                invite.getInviter().getUsername(),
                                invite.getInviter().getFirstName(),
                                invite.getInviter().getLastName(),
                                invite.getInviter().getProfilePictureUri()
                        ),
                        invite.getStatus(),
                        invite.getExpiresAt()
                )).collect(Collectors.toList());
    }
}

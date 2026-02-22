package com.boardly.service;

import com.boardly.commmon.dto.board.*;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.InviteStatus;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.mapper.UserMapper;
import com.boardly.data.model.sql.authentication.User;
import com.boardly.data.model.sql.board.Board;
import com.boardly.data.model.sql.board.BoardInvite;
import com.boardly.data.model.sql.board.BoardMember;
import com.boardly.data.model.sql.workspace.WorkspaceMember;
import com.boardly.data.repository.*;
import com.boardly.exception.BadRequestException;
import com.boardly.exception.ForbiddenException;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoardMembershipService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardInviteRepository boardInviteRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public BoardMembershipService(BoardRepository boardRepository, BoardMemberRepository boardMemberRepository, UserRepository userRepository, WorkspaceMemberRepository workspaceMemberRepository, BoardInviteRepository boardInviteRepository, UserMapper userMapper, NotificationService notificationService) {
        this.boardRepository = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.boardInviteRepository = boardInviteRepository;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public void joinBoard(UUID boardId, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (boardMemberRepository.existsByBoard_IdAndUser_Id(boardId, appUserDetails.getUserId())) {
            throw new BadRequestException("You are already a member of this board");
        }

        BoardMember newMember = new BoardMember();
        newMember.setBoard(board);
        newMember.setUser(userRepository.findById(appUserDetails.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        newMember.setRole(BoardRole.MEMBER);

        boardMemberRepository.save(newMember);
        notificationService.sendToTopic("/topic/board/" + boardId, "User " + appUserDetails.getUsername() + " joined the board");
    }

    @Transactional
    public void leaveBoard(UUID boardId, AppUserDetails appUserDetails) {
        UUID userId = appUserDetails.getUserId();
        BoardMember member = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this board"));
        long totalMembers = boardMemberRepository.countByBoard_Id(boardId);
        if (totalMembers <= 1) {
            throw new ForbiddenException("You are the last member. You must delete the board to leave, or invite someone else and make them admin first.");
        }
        if (member.getRole() == BoardRole.ADMIN) {
            long adminCount = boardMemberRepository.countByBoard_IdAndRole(boardId, BoardRole.ADMIN);
            if (adminCount <= 1) {
                throw new ForbiddenException("You are the last Admin. You must promote another member to Admin before you can leave.");
            }
        }
        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_IdAndRole(member.getBoard().getWorkspace().getId(), appUserDetails.getUserId(), WorkspaceRole.GUEST)) {
            if (boardMemberRepository.countByWorkspace_IdAndUser_Id(member.getBoard().getWorkspace().getId(), appUserDetails.getUserId()) <= 1) {
                workspaceMemberRepository.deleteByWorkspace_IdAndUser_Id(member.getBoard().getWorkspace().getId(), appUserDetails.getUserId());
            }
        }
        boardMemberRepository.delete(member);
        notificationService.sendToTopic("/topic/board/" + boardId, Map.of("type", "USER_LEFT", "userId", userId));
        notificationService.sendToUser(userId, "/queue/access-revoked", Map.of("boardId", boardId));
    }

    @Transactional
    public void removeBoardMember(UUID boardId, UUID memberId) {
        BoardMember memberToRemove = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (boardMemberRepository.countByBoard_Id(boardId) <= 1) {
            throw new ForbiddenException("Cannot remove the last member. Please delete the board instead.");
        }
        if (memberToRemove.getRole() == BoardRole.ADMIN) {
            long adminCount = boardMemberRepository.countByBoard_IdAndRole(boardId, BoardRole.ADMIN);
            if (adminCount <= 1) {
                throw new ForbiddenException("Cannot remove the last Admin. You must promote another member to Admin before you can remove them.");
            }
        }
        if (workspaceMemberRepository.existsByWorkspace_IdAndUser_IdAndRole(memberToRemove.getBoard().getWorkspace().getId(), memberToRemove.getId(), WorkspaceRole.GUEST)) {
            if (boardMemberRepository.countByWorkspace_IdAndUser_Id(memberToRemove.getBoard().getWorkspace().getId(), memberToRemove.getUser().getId()) <= 1) {
                workspaceMemberRepository.deleteByWorkspace_IdAndUser_Id(memberToRemove.getBoard().getWorkspace().getId(), memberToRemove.getUser().getId());
            }
        }
        boardMemberRepository.delete(memberToRemove);
        notificationService.sendToTopic("/topic/board/" + boardId, Map.of("type", "USER_REMOVED", "userId", memberId));
        notificationService.sendToUser(memberId, "/queue/access-revoked", Map.of("boardId", boardId));
    }

    @Transactional
    public void changeBoardMemberRole(UUID boardId, UUID memberId, BoardChangeRoleRequestDTO boardChangeRoleRequestDTO) {
        BoardRole newRole = boardChangeRoleRequestDTO.getRole();
        BoardMember targetMember = boardMemberRepository.findByBoard_IdAndUser_Id(boardId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if (targetMember.getRole() == BoardRole.ADMIN && newRole != BoardRole.ADMIN) {
            long adminCount = boardMemberRepository.countByBoard_IdAndRole(boardId, BoardRole.ADMIN);
            if (adminCount <= 1) {
                throw new ForbiddenException("Cannot remove Admin role. This user is the only Admin on the board.");
            }
        }
        targetMember.setRole(newRole);
        boardMemberRepository.save(targetMember);
        notificationService.sendToUser(memberId, "/queue/board", "Your role in the board has been changed to " + newRole);
        notificationService.sendToTopic("/topic/board/" + boardId, "User " + targetMember.getUser().getUsername() + "'s role has been changed to " + newRole);
    }

    public List<BoardMemberDTO> getBoardMembers(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
        return boardMemberRepository.findAllByBoard_Id(boardId).stream()
                .map(member -> new BoardMemberDTO(
                        userMapper.toDTO(member.getUser()),
                        member.getRole(),
                        member.getCreatedAt()
                )).collect(Collectors.toList());
    }

    // For workspace members
    @Transactional
    public BoardMemberDTO addWorkspaceMemberToBoard(UUID boardId, BoardAddWSMemberRequestDTO boardAddWSMemberRequestDTO) {
        UUID userId = boardAddWSMemberRequestDTO.getUserId();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (boardMemberRepository.existsByBoard_IdAndUser_Id(boardId, userId)) {
            throw new BadRequestException("User is already a member of this board");
        }
        boolean isWorkspaceMember = workspaceMemberRepository.existsByWorkspace_IdAndUser_Id(board.getWorkspace().getId(), userId);
        if (!isWorkspaceMember) {
            throw new BadRequestException("User is not a member of the workspace");
        }
        BoardMember newMember = new BoardMember();
        newMember.setBoard(board);
        newMember.setUser(user);
        newMember.setRole(BoardRole.MEMBER);
        boardMemberRepository.save(newMember);

        notificationService.sendToUser(userId, "/queue/board", "You have been added to the board");
        notificationService.sendToTopic("/topic/board/" + boardId, "User " + user.getUsername() + " has been added to the board");

        return new BoardMemberDTO(
                userMapper.toDTO(user),
                newMember.getRole(),
                newMember.getCreatedAt()
        );
    }

    // For non-workspace members
    @Transactional
    public BoardInviteDTO inviteToBoard(UUID boardId, BoardInviteRequestDTO boardInviteRequestDTO, AppUserDetails appUserDetails) {
        String email = boardInviteRequestDTO.getEmail();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        User userToInvite = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));

        if (boardMemberRepository.existsByBoard_IdAndUser_Id(boardId, userToInvite.getId())) {
            throw new BadRequestException("User is already a member of this board");
        }

        if (boardInviteRepository.existsByBoard_IdAndInvitee_IdAndStatus(boardId, userToInvite.getId(), InviteStatus.PENDING)) {
            throw new BadRequestException("User already has a pending invitation");
        }

        boolean workspaceMember = workspaceMemberRepository.existsByWorkspaceAndUser(board.getWorkspace(), userToInvite);
        if (workspaceMember) {
            BoardMember newMember = new BoardMember();
            newMember.setBoard(board);
            newMember.setUser(userToInvite);
            newMember.setRole(BoardRole.MEMBER);
            boardMemberRepository.save(newMember);
            notificationService.sendToUser(userToInvite.getId(), "/queue/board", "You have been added to the board");
            notificationService.sendToTopic("/topic/board/" + boardId, "User " + userToInvite.getUsername() + " has been added to the board");
            return new BoardInviteDTO(
                    null,
                    board.getId(),
                    board.getTitle(),
                    userMapper.toDTO(appUserDetails.getUser()),
                    userMapper.toDTO(userToInvite),
                    InviteStatus.ACCEPTED,
                    null
            );
        }

        BoardInvite invite = new BoardInvite();
        invite.setBoard(board);
        invite.setInvitee(userToInvite);
        invite.setInviter(appUserDetails.getUser());
        invite.setStatus(InviteStatus.PENDING);
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        boardInviteRepository.save(invite);

        BoardInviteDTO inviteDTO = new BoardInviteDTO(
                invite.getId(),
                board.getId(),
                board.getTitle(),
                userMapper.toDTO(appUserDetails.getUser()),
                userMapper.toDTO(userToInvite),
                invite.getStatus(),
                invite.getExpiresAt()
        );
        notificationService.sendToUser(userToInvite.getId(), "/queue/invites", inviteDTO);
        return inviteDTO;
    }


    public List<BoardInviteDTO> getBoardPendingInvites(UUID boardId) {
        return boardInviteRepository.findAllByBoard_IdAndStatus(boardId, InviteStatus.PENDING).stream()
                .map(invite -> new BoardInviteDTO(
                        invite.getId(),
                        userMapper.toDTO(invite.getInviter()),
                        userMapper.toDTO(invite.getInvitee()),
                        invite.getStatus(),
                        invite.getExpiresAt()
                )).collect(Collectors.toList());
    }

    public List<BoardInviteDTO> getUserBoardPendingInvites(AppUserDetails appUserDetails) {
        return boardInviteRepository.findAllByInvitee_IdAndStatus(appUserDetails.getUserId(), InviteStatus.PENDING).stream()
                .map(invite -> new BoardInviteDTO(
                        invite.getId(),
                        invite.getBoard().getId(),
                        invite.getBoard().getTitle(),
                        userMapper.toDTO(invite.getInviter()),
                        invite.getStatus(),
                        invite.getExpiresAt()
                )).collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvitationToBoard(UUID inviteId, AppUserDetails appUserDetails) {
        BoardInvite invite = boardInviteRepository.findByIdAndInvitee_Id(inviteId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invalid Invitation");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            boardInviteRepository.save(invite);
            throw new BadRequestException("Invitation has expired");
        }

        if (boardMemberRepository.existsByBoard_IdAndUser_Id(invite.getBoard().getId(), appUserDetails.getUserId())) {
            throw new BadRequestException("You are already a member of this board");
        }

        BoardMember newMember = new BoardMember();
        newMember.setBoard(invite.getBoard());
        newMember.setUser(invite.getInvitee());
        newMember.setRole(BoardRole.MEMBER);
        boardMemberRepository.save(newMember);

        if (!workspaceMemberRepository.existsByWorkspaceAndUser(invite.getBoard().getWorkspace(), invite.getInvitee())) {
            WorkspaceMember newWorkspaceMember = new WorkspaceMember();
            newWorkspaceMember.setWorkspace(invite.getBoard().getWorkspace());
            newWorkspaceMember.setUser(invite.getInvitee());
            newWorkspaceMember.setRole(WorkspaceRole.GUEST);
            workspaceMemberRepository.save(newWorkspaceMember);
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        boardInviteRepository.save(invite);
        notificationService.sendToTopic("/topic/board/" + invite.getBoard().getId(), "User " + appUserDetails.getUsername() + " joined the board");
    }

    @Transactional
    public void declineInvitationToBoard(UUID inviteId, AppUserDetails appUserDetails) {
        BoardInvite invite = boardInviteRepository.findByIdAndInvitee_Id(inviteId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }
        invite.setStatus(InviteStatus.DECLINED);
        boardInviteRepository.save(invite);
    }

    @Transactional
    public void cancelBoardInvitation(UUID inviteId) {
        BoardInvite invite = boardInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid Invitation"));
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invitation is not pending");
        }
        invite.setStatus(InviteStatus.CANCELLED);
        boardInviteRepository.save(invite);
    }
}

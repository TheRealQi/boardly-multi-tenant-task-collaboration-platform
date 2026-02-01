package com.boardly.service;

import com.boardly.commmon.dto.board.BoardChangeRoleDTO;
import com.boardly.commmon.dto.board.BoardInviteDTO;
import com.boardly.commmon.dto.board.BoardMemberDTO;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.InviteStatus;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.mapper.UserMapper;
import com.boardly.data.model.authentication.User;
import com.boardly.data.model.board.Board;
import com.boardly.data.model.board.BoardInvite;
import com.boardly.data.model.board.BoardMember;
import com.boardly.data.model.workspace.WorkspaceMember;
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

    public BoardMembershipService(BoardRepository boardRepository, BoardMemberRepository boardMemberRepository, UserRepository userRepository, WorkspaceMemberRepository workspaceMemberRepository, BoardInviteRepository boardInviteRepository, UserMapper userMapper) {
        this.boardRepository = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.boardInviteRepository = boardInviteRepository;
        this.userMapper = userMapper;
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
        boardMemberRepository.delete(member);
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
        boardMemberRepository.delete(memberToRemove);
    }

    @Transactional
    public void changeBoardMemberRole(UUID boardId, UUID memberId, BoardChangeRoleDTO boardChangeRoleDTO) {
        BoardRole newRole = boardChangeRoleDTO.getRole();
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
    }

    public List<BoardMemberDTO> getBoardMembers(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
        return boardMemberRepository.findAllByBoard_Id(boardId).stream()
                .map(member -> new BoardMemberDTO(
                        boardId,
                        userMapper.toDTO(member.getUser()),
                        member.getRole(),
                        member.getCreatedAt()
                )).collect(Collectors.toList());
    }

    @Transactional
    public BoardMemberDTO addWorkspaceMemberToBoard(UUID boardId, UUID userId) {
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

        return new BoardMemberDTO(
                boardId,
                userMapper.toDTO(user),
                newMember.getRole(),
                newMember.getCreatedAt()
        );
    }

    // For non-workspace members
    @Transactional
    public BoardInviteDTO inviteToBoard(UUID boardId, String email, AppUserDetails appUserDetails) {
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

        BoardInvite invite = new BoardInvite();
        invite.setBoard(board);
        invite.setInvitee(userToInvite);
        invite.setInviter(appUserDetails.getUser());
        invite.setStatus(InviteStatus.PENDING);
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        boardInviteRepository.save(invite);

        return new BoardInviteDTO(
                invite.getId(),
                board.getId(),
                board.getTitle(),
                userMapper.toDTO(userToInvite),
                userMapper.toDTO(appUserDetails.getUser()),
                invite.getStatus(),
                invite.getExpiresAt()
        );
    }


    public List<BoardInviteDTO> getBoardPendingInvites(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        return boardInviteRepository.findAllByBoard_IdAndStatus(boardId, InviteStatus.PENDING).stream()
                .map(invite -> new BoardInviteDTO(
                        invite.getId(),
                        invite.getBoard().getId(),
                        board.getTitle(),
                        userMapper.toDTO(invite.getInvitee()),
                        userMapper.toDTO(invite.getInviter()),
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
                        userMapper.toDTO(invite.getInvitee()),
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

        WorkspaceMember newWorkspaceMember = new WorkspaceMember();
        newWorkspaceMember.setWorkspace(invite.getBoard().getWorkspace());
        newWorkspaceMember.setUser(invite.getInvitee());
        newWorkspaceMember.setRole(WorkspaceRole.GUEST);
        workspaceMemberRepository.save(newWorkspaceMember);

        invite.setStatus(InviteStatus.ACCEPTED);
        boardInviteRepository.save(invite);
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
}

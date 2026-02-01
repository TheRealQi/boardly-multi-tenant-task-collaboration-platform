package com.boardly.service;

import com.boardly.commmon.dto.board.BoardChangeVisibilityDTO;
import com.boardly.commmon.dto.board.BoardCreationDTO;
import com.boardly.commmon.dto.board.BoardDTO;
import com.boardly.commmon.dto.board.BoardEditDTO;
import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.BoardVisibility;
import com.boardly.commmon.enums.InviteStatus;
import com.boardly.commmon.enums.WorkspaceRole;
import com.boardly.data.model.board.Board;
import com.boardly.data.model.board.BoardMember;
import com.boardly.data.repository.*;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public BoardService(BoardRepository boardRepository, BoardMemberRepository boardMemberRepository, WorkspaceMemberRepository workspaceMemberRepository) {
        this.boardRepository = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Transactional
    public BoardDTO createBoard(BoardCreationDTO boardCreationDTO, AppUserDetails appUserDetails) {
        String title = boardCreationDTO.getTitle();
        String description = boardCreationDTO.getDescription();
        BoardVisibility boardVisibility = boardCreationDTO.getBoardVisibility();

        Board board = new Board();
        board.setTitle(title);
        board.setDescription(description);
        board.setBoardVisibility(boardVisibility);

        Board savedBoard = boardRepository.save(board);

        BoardMember boardMember = new BoardMember();
        boardMember.setBoard(savedBoard);
        boardMember.setUser(appUserDetails.getUser());
        boardMember.setRole(BoardRole.ADMIN);

        boardMemberRepository.save(boardMember);

        WorkspaceRole workspaceRole = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(board.getWorkspace().getId(), appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace membership not found"));

        WorkspaceDTO workspaceDTO = new WorkspaceDTO();
        workspaceDTO.setWorkspaceId(board.getWorkspace().getId());
        workspaceDTO.setTitle(board.getWorkspace().getTitle());
        workspaceDTO.setDescription(board.getWorkspace().getDescription());
        workspaceDTO.setRole(workspaceRole);

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle(savedBoard.getTitle());
        boardDTO.setDescription(savedBoard.getDescription());
        boardDTO.setBoardVisibility(savedBoard.getBoardVisibility());
        boardDTO.setBoardRole(boardMember.getRole());
        boardDTO.setWorkspace(workspaceDTO);

        return boardDTO;
    }

    @Transactional
    public void deleteBoard(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found");
        }
        boardRepository.deleteById(boardId);
    }

    public BoardDTO getBoard(UUID boardId, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        BoardRole boardRole = boardMemberRepository.findRoleByBoardIdAndUserId(boardId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Board membership not found"));
        WorkspaceRole workspaceRole = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(board.getWorkspace().getId(), appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace membership not found"));

        WorkspaceDTO workspaceDTO = new WorkspaceDTO();
        workspaceDTO.setWorkspaceId(board.getWorkspace().getId());
        workspaceDTO.setTitle(board.getWorkspace().getTitle());
        workspaceDTO.setDescription(board.getWorkspace().getDescription());
        workspaceDTO.setRole(workspaceRole);

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle(board.getTitle());
        boardDTO.setDescription(board.getDescription());
        boardDTO.setBoardVisibility(board.getBoardVisibility());
        boardDTO.setBoardRole(boardRole);
        boardDTO.setWorkspace(workspaceDTO);
        return boardDTO;
    }

    public BoardDTO editBoard(UUID boardId, BoardEditDTO boardEditDTO, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        BoardRole boardRole = boardMemberRepository.findRoleByBoardIdAndUserId(boardId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Board membership not found"));

        if (boardRole != BoardRole.ADMIN) {
            throw new SecurityException("Only admins can update the board");
        }

        board.setTitle(boardEditDTO.getTitle());
        board.setDescription(boardEditDTO.getDescription());

        Board updatedBoard = boardRepository.save(board);

        WorkspaceRole workspaceRole = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(board.getWorkspace().getId(), appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace membership not found"));

        WorkspaceDTO workspaceDTO = new WorkspaceDTO();
        workspaceDTO.setWorkspaceId(board.getWorkspace().getId());
        workspaceDTO.setTitle(board.getWorkspace().getTitle());
        workspaceDTO.setDescription(board.getWorkspace().getDescription());
        workspaceDTO.setRole(workspaceRole);

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle(updatedBoard.getTitle());
        boardDTO.setDescription(updatedBoard.getDescription());
        boardDTO.setBoardVisibility(updatedBoard.getBoardVisibility());
        boardDTO.setBoardRole(boardRole);
        boardDTO.setWorkspace(workspaceDTO);

        return boardDTO;
    }

    public BoardDTO changeBoardVisibility(UUID boardId, BoardChangeVisibilityDTO boardChangeVisibilityDTO, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        BoardRole boardRole = boardMemberRepository.findRoleByBoardIdAndUserId(boardId, appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Board membership not found"));

        if (boardRole != BoardRole.ADMIN) {
            throw new SecurityException("Only admins can change board visibility");
        }

        board.setBoardVisibility(boardChangeVisibilityDTO.getBoardVisibility());

        Board updatedBoard = boardRepository.save(board);

        WorkspaceRole workspaceRole = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(board.getWorkspace().getId(), appUserDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace membership not found"));

        WorkspaceDTO workspaceDTO = new WorkspaceDTO();
        workspaceDTO.setWorkspaceId(board.getWorkspace().getId());
        workspaceDTO.setTitle(board.getWorkspace().getTitle());
        workspaceDTO.setDescription(board.getWorkspace().getDescription());
        workspaceDTO.setRole(workspaceRole);

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle(updatedBoard.getTitle());
        boardDTO.setDescription(updatedBoard.getDescription());
        boardDTO.setBoardVisibility(updatedBoard.getBoardVisibility());
        boardDTO.setBoardRole(boardRole);
        boardDTO.setWorkspace(workspaceDTO);

        return boardDTO;
    }
}

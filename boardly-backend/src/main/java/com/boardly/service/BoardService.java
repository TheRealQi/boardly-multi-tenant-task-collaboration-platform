package com.boardly.service;

import com.boardly.commmon.dto.board.BoardChangeVisibilityRequestDTO;
import com.boardly.commmon.dto.board.BoardCreationRequestDTO;
import com.boardly.commmon.dto.board.BoardDTO;
import com.boardly.commmon.dto.board.BoardEditRequestDTO;
import com.boardly.commmon.dto.workspace.WorkspaceDTO;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.data.mapper.BoardMapper;
import com.boardly.data.mapper.WorkspaceMapper;
import com.boardly.data.model.sql.board.Board;
import com.boardly.data.model.sql.board.BoardMember;
import com.boardly.data.model.sql.workspace.Workspace;
import com.boardly.data.repository.*;
import com.boardly.exception.ResourceNotFoundException;
import com.boardly.security.model.AppUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardMapper boardMapper;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMapper workspaceMapper;
    private final KanbanBoardService kanbanBoardService;

    public BoardService(BoardRepository boardRepository, BoardMemberRepository boardMemberRepository, WorkspaceMemberRepository workspaceMemberRepository, BoardMapper boardMapper, WorkspaceRepository workspaceRepository, WorkspaceMapper workspaceMapper, KanbanBoardService kanbanBoardService) {
        this.boardRepository = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.boardMapper = boardMapper;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMapper = workspaceMapper;
        this.kanbanBoardService = kanbanBoardService;
    }

    @Transactional
    public BoardDTO createBoard(BoardCreationRequestDTO boardCreationRequestDTO, AppUserDetails appUserDetails) {
        Board board = boardMapper.toEntity(boardCreationRequestDTO);

        Workspace workspace = workspaceRepository.findById(boardCreationRequestDTO.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        board.setWorkspace(workspace);

        Board savedBoard = boardRepository.save(board);
        BoardMember boardMember = new BoardMember();
        boardMember.setBoard(savedBoard);
        boardMember.setUser(appUserDetails.getUser());
        boardMember.setRole(BoardRole.ADMIN);
        boardMemberRepository.save(boardMember);

        WorkspaceDTO workspaceDTO = workspaceRepository.findWorkspaceDTOByWorkspaceAndUser(board.getWorkspace(), appUserDetails.getUser()).orElseThrow(() -> new ResourceNotFoundException("Workspace doesnt exist or user is not a member of the workspace"));

        kanbanBoardService.createBoard(savedBoard.getId());

        return boardMapper.toDto(savedBoard, boardMember.getRole(), workspaceDTO);
    }

    @Transactional
    public void deleteBoard(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        boardRepository.delete(board);
        kanbanBoardService.deleteBoard(boardId);
    }

    public BoardDTO getBoard(UUID boardId, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        BoardRole boardRole = boardMemberRepository.findRoleByBoardIdAndUserId(boardId, appUserDetails.getUserId())
                .orElse(BoardRole.VIWER);

        WorkspaceDTO workspaceDTO = workspaceRepository.findWorkspaceDTOByWorkspaceAndUser(board.getWorkspace(), appUserDetails.getUser()).orElseThrow(() -> new ResourceNotFoundException("Workspace doesnt exist or user is not a member of the workspace"));

        return boardMapper.toDto(board, boardRole, workspaceDTO);
    }

    public List<BoardDTO> getBoardsForUser(AppUserDetails appUserDetails) {
        return boardRepository.findAllBoardDTOsByUser(appUserDetails.getUserId());
    }

    public List<BoardDTO> getBoardsForWorkspace(UUID workspaceId, AppUserDetails appUserDetails) {
        return boardRepository.findAllViewableBoardDTOsByUserAndWorkspace(workspaceId, appUserDetails.getUserId());
    }

    public void editBoard(UUID boardId, BoardEditRequestDTO boardEditRequestDTO, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        board.setTitle(boardEditRequestDTO.getTitle());
        board.setDescription(boardEditRequestDTO.getDescription());
        boardRepository.save(board);
    }

    public void changeBoardVisibility(UUID boardId, BoardChangeVisibilityRequestDTO boardChangeVisibilityRequestDTO, AppUserDetails appUserDetails) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        board.setBoardVisibility(boardChangeVisibilityRequestDTO.getBoardVisibility());
        boardRepository.save(board);
    }


}

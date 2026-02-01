package com.boardly.commmon.dto.board;

import com.boardly.commmon.dto.UserDTO;
import com.boardly.commmon.enums.BoardRole;
import com.boardly.commmon.enums.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardMemberDTO {
    UUID id;
    UserDTO user;
    BoardRole role;
    Instant joinedAt;
}

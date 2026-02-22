package com.boardly.common.dto.board;

import com.boardly.common.dto.UserDTO;
import com.boardly.common.enums.BoardRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardMemberDTO {
    UserDTO user;
    BoardRole role;
    Instant joinedAt;
}

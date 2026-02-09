package com.boardly.commmon.dto.board;

import com.boardly.commmon.dto.UserDTO;
import com.boardly.commmon.enums.BoardRole;
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

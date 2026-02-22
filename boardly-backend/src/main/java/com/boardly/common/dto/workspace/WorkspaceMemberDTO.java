package com.boardly.common.dto.workspace;

import com.boardly.common.dto.UserDTO;
import com.boardly.common.enums.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMemberDTO {
    UserDTO user;
    WorkspaceRole role;
    Instant joinedAt;
}

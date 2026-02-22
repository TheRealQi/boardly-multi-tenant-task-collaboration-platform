package com.boardly.data.model.sql.board;

import com.boardly.commmon.enums.BoardRole;
import com.boardly.data.model.sql.BaseEntity;
import com.boardly.data.model.sql.authentication.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BoardMember extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_Id")
    private Board board;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id")
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BoardRole role = BoardRole.MEMBER;
}

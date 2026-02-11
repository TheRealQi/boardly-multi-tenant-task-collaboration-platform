package com.boardly.commmon.dto.kanbanboard;

import com.boardly.commmon.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CardCommentDTO {
    private UserDTO author;
    private String comment;
    private Instant date;
    private boolean edited;
}

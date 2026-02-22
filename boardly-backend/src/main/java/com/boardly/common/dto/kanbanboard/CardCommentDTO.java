package com.boardly.common.dto.kanbanboard;

import com.boardly.common.dto.UserDTO;
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
public class CardCommentDTO {
    private UUID id;
    private UserDTO author;
    private String comment;
    private Instant date;
    private boolean edited;
}

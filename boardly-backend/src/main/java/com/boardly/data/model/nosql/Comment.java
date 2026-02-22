package com.boardly.data.model.nosql;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class Comment {
    private String Id = UUID.randomUUID().toString();
    private String authorId;
    private String content;
    private boolean edited = false;
    private Instant createdAt = Instant.now();
}

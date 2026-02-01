package com.boardly.commmon.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardEditDTO {
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @NotBlank(message = "Title is required")
    private String title;
    @Size(max = 1024, message = "Description must be at most 1024 characters")
    private String description;
}

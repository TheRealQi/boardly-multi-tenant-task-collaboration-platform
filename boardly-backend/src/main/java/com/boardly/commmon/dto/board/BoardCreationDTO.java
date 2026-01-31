package com.boardly.commmon.dto.board;

import com.boardly.commmon.enums.BoardVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BoardCreationDTO {
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @NotBlank(message = "Title is required")
    private String title;
    @Size(max = 1024, message = "Description must be at most 1024 characters")
    private String description;
    @NotBlank(message = "Board visibility is required")
    private BoardVisibility boardVisibility;

    @NotNull(message = "Workspace is required")
    private UUID workspaceId;
}

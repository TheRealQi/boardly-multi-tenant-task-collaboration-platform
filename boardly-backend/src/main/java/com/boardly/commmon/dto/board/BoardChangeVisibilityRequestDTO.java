package com.boardly.commmon.dto.board;

import com.boardly.commmon.enums.BoardVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardChangeVisibilityRequestDTO {
    @NotNull(message = "Board visibility is required")
    private BoardVisibility boardVisibility;
}

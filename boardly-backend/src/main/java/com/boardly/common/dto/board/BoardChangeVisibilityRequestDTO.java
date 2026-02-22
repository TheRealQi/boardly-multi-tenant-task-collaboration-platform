package com.boardly.common.dto.board;

import com.boardly.common.enums.BoardVisibility;
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

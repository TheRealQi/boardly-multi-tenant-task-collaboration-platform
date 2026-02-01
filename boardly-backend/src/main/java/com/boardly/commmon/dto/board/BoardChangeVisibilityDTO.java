package com.boardly.commmon.dto.board;

import com.boardly.commmon.enums.BoardVisibility;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardChangeVisibilityDTO {
    @NotBlank(message = "Visibility is required")
    private BoardVisibility boardVisibility;
}

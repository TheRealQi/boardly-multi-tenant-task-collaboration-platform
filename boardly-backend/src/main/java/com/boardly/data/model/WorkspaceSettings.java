package com.boardly.data.model;

import com.boardly.commmon.enums.BoardCreationSetting;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Embeddable
public class WorkspaceSettings {
    @Enumerated(EnumType.STRING)
    private BoardCreationSetting workspaceVisibleBoardCreation = BoardCreationSetting.ANY_MEMBER;

    @Enumerated(EnumType.STRING)
    private BoardCreationSetting privateBoardCreation = BoardCreationSetting.ANY_MEMBER;
}

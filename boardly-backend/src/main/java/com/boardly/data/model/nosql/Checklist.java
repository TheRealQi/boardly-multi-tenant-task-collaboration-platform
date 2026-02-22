package com.boardly.data.model.nosql;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class Checklist {
    private String Id = UUID.randomUUID().toString();
    private String title;
    private List<ChecklistItem> items = new ArrayList<>();
}

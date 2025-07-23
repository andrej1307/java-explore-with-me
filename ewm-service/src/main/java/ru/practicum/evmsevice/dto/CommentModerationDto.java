package ru.practicum.evmsevice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.evmsevice.enums.CommentState;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class CommentModerationDto {
    private List<Integer> commentIds = new ArrayList<>();
    private CommentState state;
}

package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.CommentModerationDto;
import ru.practicum.evmsevice.dto.CommentsGroupDto;
import ru.practicum.evmsevice.dto.NewCommentDto;
import ru.practicum.evmsevice.enums.CommentState;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Integer userId, Integer eventId, NewCommentDto commentDto);

    CommentDto updateComment(Integer userId, Integer commentId, NewCommentDto commentDto);

    CommentsGroupDto moderationComments(Integer userId, Integer eventId, CommentModerationDto commentModerationDto);

    CommentDto getCommentById(Integer commentId);

    List<CommentDto> getCommentsByEventId(Integer eventId,
                                          String text,
                                          List<Integer> authorIds,
                                          String rangeStart,
                                          String rangeEnd,
                                          CommentState state,
                                          String sort,
                                          Integer from,
                                          Integer size);

    List<CommentDto> getCommentsByUserId(Integer userId, Integer from, Integer size);

    void deleteComment(Integer userId, Integer commentId);
}

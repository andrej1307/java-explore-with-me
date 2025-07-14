package ru.practicum.evmsevice.service;

import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Integer userId, Integer eventId, NewCommentDto commentDto);
    CommentDto updateComment(Integer userId, Integer commentId, NewCommentDto commentDto);
    CommentDto getCommentById(Integer commentId);
    List<CommentDto> getCommentsByEventId(Integer eventId);
    List<CommentDto> getCommentsByUserId(Integer userId);
}

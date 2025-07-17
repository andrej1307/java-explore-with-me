package ru.practicum.evmsevice.mapper;

import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.NewCommentDto;
import ru.practicum.evmsevice.model.Comment;

import java.time.LocalDateTime;

public class CommentMapper {
    private CommentMapper() {
    }

    public static Comment getComment(NewCommentDto newCommemtDto) {
        Comment comment = new Comment();
        comment.setCreatedOn(LocalDateTime.now());
        comment.setText(newCommemtDto.getText());
        return comment;
    }

    public static CommentDto toDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setEventId(comment.getEventId());
        commentDto.setText(comment.getText());
        commentDto.setCreatedOn(comment.getCreatedOn());
        commentDto.setEditedOn(comment.getEditedOn());
        commentDto.setAuthor(UserMapper.toUserDto(comment.getAuthor()));
        commentDto.setState(comment.getState());
        return commentDto;
    }
}

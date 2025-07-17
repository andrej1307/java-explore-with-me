package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.CommentModerationDto;
import ru.practicum.evmsevice.dto.CommentsGroupDto;
import ru.practicum.evmsevice.dto.NewCommentDto;
import ru.practicum.evmsevice.enums.CommentState;
import ru.practicum.evmsevice.exception.DataConflictException;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.exception.ValidationException;
import ru.practicum.evmsevice.mapper.CommentMapper;
import ru.practicum.evmsevice.model.Comment;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.CommentRepository;
import ru.practicum.evmsevice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final UserService userService;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDto addComment(Integer userId, Integer eventId, NewCommentDto commentDto) {
        User user = userService.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие id=" + eventId));
        Comment comment = CommentMapper.getComment(commentDto);
        comment.setAuthor(user);
        comment.setEventId(eventId);
        comment.setState(CommentState.PENDING);
        comment.setCreatedOn(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Integer userId, Integer commentId, NewCommentDto commentDto) {
        User user = userService.getUserById(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Не найден комментарий id=" + commentId));
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ValidationException("Редактировать комментарий может только его автор.");
        }
        comment.setText(commentDto.getText());
        comment.setEditedOn(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    /**
     * Модерация комментариев к событию
     * @param userId - идентификатор инициатора события
     * @param eventId - идентификатор события
     * @param commentModerationDto - объект идентификаторов коментариев для модерации
     * @return - список комментариев с измененным статусом
     */
    @Override
    @Transactional
    public CommentsGroupDto moderationComments(Integer userId, Integer eventId, CommentModerationDto commentModerationDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие id=" + eventId));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException(String.format(
                    "Польдователь id=%d не является инициатором события id=%d.",
                    userId, eventId));
        }
        List<Comment> comments = commentRepository.findAllById(commentModerationDto.getCommentIds());
        CommentsGroupDto cgDto = new CommentsGroupDto();
        CommentState commentState = commentModerationDto.getState();
        for (Comment comment : comments) {
            if (!comment.getEventId().equals(eventId)) {
                throw new DataConflictException(String.format(
                        "Комментарий id=%d не относится к событию id=%d.",
                        comment.getId(), eventId));
            }
            // меняем состояние только у комментариев ожидающих модерацмм
            if (comment.getState().equals(CommentState.PENDING)) {
                comment.setState(commentState);
            }
            Comment savedComment = commentRepository.save(comment);
            CommentDto commentDto = CommentMapper.toDto(savedComment);
            if (commentDto.getState().equals(CommentState.APPROVED)) {
                cgDto.getApprovedComments().add(commentDto);
            }
            if (commentDto.getState().equals(CommentState.REJECTED)) {
                cgDto.getRejectedComments().add(commentDto);
            }
        }
        return cgDto;
    }

    @Override
    public CommentDto getCommentById(Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Не найден комментарий id=" + commentId));
        return CommentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventId(Integer eventId, Integer from, Integer ize) {
        return commentRepository.findAllByEventId(eventId).stream()
                .map(CommentMapper::toDto)
                .skip(from)
                .limit(ize)
                .toList();
    }

    @Override
    public List<CommentDto> getCommentsByUserId(Integer userId, Integer from, Integer ize) {
        return commentRepository.findAllByAuthor_Id(userId).stream()
                .map(CommentMapper::toDto)
                .skip(from)
                .limit(ize)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Integer userId, Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Не найден комментарий id=" + commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException(String.format(
                    "Пользователь id=%d не является автором комментария id=%d.",
                    userId, commentId
            ));
        }
        commentRepository.delete(comment);
    }
}

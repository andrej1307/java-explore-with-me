package ru.practicum.evmsevice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.CommentModerationDto;
import ru.practicum.evmsevice.dto.CommentsGroupDto;
import ru.practicum.evmsevice.dto.NewCommentDto;
import ru.practicum.evmsevice.enums.CommentState;
import ru.practicum.evmsevice.enums.EventState;
import ru.practicum.evmsevice.exception.BadRequestException;
import ru.practicum.evmsevice.exception.DataConflictException;
import ru.practicum.evmsevice.exception.NotFoundException;
import ru.practicum.evmsevice.exception.ValidationException;
import ru.practicum.evmsevice.mapper.CommentMapper;
import ru.practicum.evmsevice.model.Comment;
import ru.practicum.evmsevice.model.Event;
import ru.practicum.evmsevice.model.User;
import ru.practicum.evmsevice.repository.CommentRepository;
import ru.practicum.evmsevice.repository.CommentSpecification;
import ru.practicum.evmsevice.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Integer COMMENT_PERIOD_HOURS = 72;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    /**
     * Создаем новый комментарий
     *
     * @param userId     - идентификатор пользователя
     * @param eventId    - идентификатор события
     * @param commentDto - входящий объект комметнария
     * @return - сохраненный объект комментария
     */
    @Override
    @Transactional
    public CommentDto addComment(Integer userId, Integer eventId, NewCommentDto commentDto) {
        User user = userService.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено событие id=" + eventId));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Комментировать возможно только опубликованные события.");
        }
        if (event.getEventDate().plusHours(COMMENT_PERIOD_HOURS).isBefore(LocalDateTime.now())) {
            throw new ValidationException("Комментировать событие возможно только в течении " +
                    COMMENT_PERIOD_HOURS + " часов от начала события.");
        }
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
     *
     * @param userId               - идентификатор инициатора события
     * @param eventId              - идентификатор события
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
    public List<CommentDto> getCommentsByEventId(Integer eventId,
                                                 String text,
                                                 List<Integer> authorIds,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 CommentState state,
                                                 String sort,
                                                 Integer from,
                                                 Integer size) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (rangeStart != null && !rangeStart.isEmpty()) {
            try {
                startDateTime = LocalDateTime.parse(rangeStart, DATA_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Некорректный формат времени. " + e.getMessage());
            }
        }
        if (rangeEnd != null && !rangeEnd.isEmpty()) {
            try {
                endDateTime = LocalDateTime.parse(rangeEnd, DATA_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Некорректный формат времени. " + e.getMessage());
            }
        }
        if (startDateTime != null && endDateTime != null) {
            if (startDateTime.isAfter(endDateTime)) {
                throw new BadRequestException(
                        "Parameter: rangeStart, rangeEnd. " +
                                "Error: Введен некорректный интервал времени." +
                                ". Value: " + startDateTime.format(DATA_TIME_FORMATTER) +
                                ", " + endDateTime.format(DATA_TIME_FORMATTER)
                );
            }
        }

        Specification<Comment> spec = Specification.where(null);
        // Задаем спецификации для поиска комментариев к событию
        spec = spec.and(CommentSpecification.commentEventIdEqual(eventId));
        // ...поиск Комментариев по тексту
        if (text != null) {
            spec = spec.and(CommentSpecification.commentContains(text));
        }
        // ... поиск по списку идентификаторов авторов комментариев
        if (authorIds != null) {
            spec = spec.and(CommentSpecification.commentAuthorIdIn(authorIds));
        }
        // поиск по времени создания комментария
        if (startDateTime != null) {
            spec = spec.and(CommentSpecification.commentCreatedAfter(startDateTime));
        }
        if (endDateTime != null) {
            spec = spec.and(CommentSpecification.commentCreatedBefore(endDateTime));
        }
        spec = spec.and(CommentSpecification.commentStateEqual(state));
        List<Comment> comments = new ArrayList<>();
        if (sort != null) {
            if (sort.equalsIgnoreCase("OLD")) {
                comments = commentRepository.findAll(spec, Sort.by("createdOn"));
            } else {
                comments = commentRepository.findAll(spec, Sort.by("createdOn").descending());
            }
        } else {
            comments = commentRepository.findAll(spec, Sort.by("createdOn").descending());
        }

        return comments.stream()
                .map(CommentMapper::toDto)
                .skip(from)
                .limit(size)
                .toList();
    }

    @Override
    public List<CommentDto> getCommentsByUserId(Integer userId, Integer from, Integer size) {
        return commentRepository.findAllByAuthor_Id(userId).stream()
                .map(CommentMapper::toDto)
                .skip(from)
                .limit(size)
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

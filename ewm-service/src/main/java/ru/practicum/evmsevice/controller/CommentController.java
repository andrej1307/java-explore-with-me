package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.NewCommentDto;
import ru.practicum.evmsevice.service.CommentService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addNewComment(@PathVariable Integer userId,
                                    @PathVariable Integer eventId,
                                    @RequestBody @Validated NewCommentDto commentDto) {
        log.info("Пользователь id={} добавляет комментарий к событию id={}. {}",
                userId, eventId, commentDto.toString());
        return commentService.addComment(userId, eventId, commentDto);
    }

    @PatchMapping("/users/{userId}/patch/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Integer userId,
                                    @PathVariable Integer commentId,
                                    @RequestBody @Validated NewCommentDto commentDto) {
        log.info("Пользователь id={} редактирует комментарий id={}. {}",
                userId, commentId, commentDto.toString());
        return commentService.updateComment(userId, commentId, commentDto);
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByUserId(@PathVariable Integer userId) {
        log.info("Поиск всех коментариев пользователя id={}.", userId);
        return commentService.getCommentsByUserId(userId);
    }

    @GetMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByEventId(@PathVariable Integer eventId) {
        log.info("Поиск всех коментариев к событию id={}.", eventId);
        return commentService.getCommentsByEventId(eventId);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getComment(@PathVariable Integer commentId) {
        log.info("Поиск коментария id={}.", commentId);
        return commentService.getCommentById(commentId);
    }

    @DeleteMapping("/{commentId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Integer commentId,
                              @PathVariable Integer userId) {

        log.info("Пользователь id={} удаляет комментарий id={}.", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }
}

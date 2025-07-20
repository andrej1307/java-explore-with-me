package ru.practicum.evmsevice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.evmsevice.dto.CommentDto;
import ru.practicum.evmsevice.dto.CommentModerationDto;
import ru.practicum.evmsevice.dto.CommentsGroupDto;
import ru.practicum.evmsevice.dto.NewCommentDto;
import ru.practicum.evmsevice.service.CommentService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserCommentController {
    private final CommentService commentService;

    @PostMapping("/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addNewComment(@PathVariable Integer userId,
                                    @PathVariable Integer eventId,
                                    @RequestBody @Validated NewCommentDto commentDto) {
        log.info("Пользователь id={} добавляет комментарий к событию id={}. {}",
                userId, eventId, commentDto.toString());
        return commentService.addComment(userId, eventId, commentDto);
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Integer userId,
                                    @PathVariable Integer commentId,
                                    @RequestBody @Validated NewCommentDto commentDto) {
        log.info("Пользователь id={} редактирует комментарий id={}. {}",
                userId, commentId, commentDto.toString());
        return commentService.updateComment(userId, commentId, commentDto);
    }

    @PatchMapping("/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public CommentsGroupDto addNewComment(@PathVariable Integer userId,
                                          @PathVariable Integer eventId,
                                          @RequestBody CommentModerationDto cmModDto) {
        log.info("Пользователь id={}модерирует комментарии к событию id={}. {}",
                userId, eventId, cmModDto.toString());
        return commentService.moderationComments(userId, eventId, cmModDto);
    }


    @GetMapping("/{userId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByUserId(@PathVariable Integer userId,
                                                @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Поиск всех комментариев пользователя id={}.", userId);
        return commentService.getCommentsByUserId(userId, from, size);
    }


    @DeleteMapping("/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Integer commentId,
                              @PathVariable Integer userId) {

        log.info("Пользователь id={} удаляет комментарий id={}.", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }
}

package ru.practicum.evmsevice.enums;

import java.util.Optional;

public enum CommentState {
    PENDING,
    APPROVED,
    REJECTED;

    public static Optional<CommentState> from(String state) {
        for (CommentState commentState : CommentState.values()) {
            if (commentState.name().equalsIgnoreCase(state)) {
                return Optional.of(commentState);
            }
        }
        return Optional.empty();
    }
}

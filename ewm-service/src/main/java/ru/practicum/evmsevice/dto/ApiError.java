package ru.practicum.evmsevice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors = new ArrayList<>();
}

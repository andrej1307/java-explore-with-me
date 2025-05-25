package ru.practicum.evmsevice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ApiError {
    private HttpStatus status;
    private String reason;
    private String message;
    private LocalDateTime timestamp;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<String> errors = new ArrayList<>();
}

package ru.practicum.statclient;


import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface StatClient {
    ResponseEntity<Object> get();

    ResponseEntity<Object> get(@Nullable Map<String, Object> parameters);

    <T> ResponseEntity<Object> post(T body);
}

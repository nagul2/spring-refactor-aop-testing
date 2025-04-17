package org.example.expert.domain.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ErrorDto {
    private final int code;
    private final HttpStatus error;
    private final String message;
    private final LocalDateTime errorTime;
    private final String path;
}

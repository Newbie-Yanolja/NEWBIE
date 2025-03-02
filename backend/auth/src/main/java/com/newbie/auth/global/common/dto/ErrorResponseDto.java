package com.newbie.auth.global.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {

    private int status;
    private String errorMessage;
    private LocalDateTime timestamp;
}

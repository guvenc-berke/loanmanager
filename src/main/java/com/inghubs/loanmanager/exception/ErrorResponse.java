package com.inghubs.loanmanager.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private String message;
}

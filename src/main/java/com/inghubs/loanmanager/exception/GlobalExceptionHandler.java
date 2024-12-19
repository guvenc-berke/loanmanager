package com.inghubs.loanmanager.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleException(final ServiceException exception) {
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getErrorMessage())
                .build();

        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
    }
}

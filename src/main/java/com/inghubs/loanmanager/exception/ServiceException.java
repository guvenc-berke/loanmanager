package com.inghubs.loanmanager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ServiceException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorMessage;


}

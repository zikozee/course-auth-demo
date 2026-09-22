package com.zee.courseauthdemo.usermanagement.exception;


import com.zee.courseauthdemo.dto.ApiResponse;
import com.zee.courseauthdemo.util.AuthUtil;
import com.zee.courseauthdemo.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSourceUtil messageSourceUtil;

    @ExceptionHandler(UserServiceException.class)
    public final ResponseEntity<ApiResponse<?>> onAuthServiceException(UserServiceException ex) {
        ApiResponse<?> errorResponse = AuthUtil.parseExceptionInfoToResponseDto(
                messageSourceUtil, ex.getErrorInfo());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

}
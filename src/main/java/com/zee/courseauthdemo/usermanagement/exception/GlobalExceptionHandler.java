package com.zee.courseauthdemo.usermanagement.exception;


import com.zee.courseauthdemo.datatype.ErrorCodeConstants;
import com.zee.courseauthdemo.dto.ApiResponse;
import com.zee.courseauthdemo.dto.AuthFieldError;
import com.zee.courseauthdemo.util.AuthUtil;
import com.zee.courseauthdemo.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<String>> onMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        List<AuthFieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .distinct()
                .map(e -> new AuthFieldError(ErrorCodeConstants.INVALID_USER_INPUT, e.getField(), e.getDefaultMessage(), e.getRejectedValue())
                ).toList();

        ApiResponse<String> appResponse = ApiResponse.<String>builder()
                .successful(false)
                .fieldErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(appResponse, HttpStatus.BAD_REQUEST);
    }


}
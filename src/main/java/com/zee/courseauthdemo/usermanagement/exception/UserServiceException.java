package com.zee.courseauthdemo.usermanagement.exception;

import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.exception.ErrorInfo;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserServiceException extends RuntimeException {

    private final ErrorInfo errorInfo;
    private final HttpStatus status;

    public UserServiceException(ErrorInfo errorInfo, HttpStatus status) {
        super(errorInfo.getDetailedDescription());
        this.errorInfo = errorInfo;
        this.status = status;
    }

    public UserServiceException(MessageType messageType, String errorCode, HttpStatus status) {
        super("");
        this.errorInfo = ErrorInfo.builder()
                .messageType(messageType)
                .errorCode(errorCode)
                .build();
        this.status = status;
    }

    public UserServiceException(MessageType messageType, String errorCode, String desc, HttpStatus status) {
        super("");
        this.errorInfo = ErrorInfo.builder()
                .messageType(messageType)
                .errorCode(errorCode)
                .detailedDescription(desc)
                .build();
        this.status = status;
    }
}
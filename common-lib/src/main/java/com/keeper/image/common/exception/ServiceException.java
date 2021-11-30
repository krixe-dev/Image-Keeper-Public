package com.keeper.image.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for services
 */
@Getter
public class ServiceException extends RuntimeException {

    private ErrorType errorType = ErrorType.DEFAULT;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * Error types taht can be used in order to detail exception
     * Error code is used to build REST service response message
     */
    @Getter
    @AllArgsConstructor
    public enum ErrorType {
        HANDLE_FILE_EXCPETION(HttpStatus.INTERNAL_SERVER_ERROR),
        IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND),
        IMAGE_STATUS_INVALID(HttpStatus.BAD_REQUEST),
        USER_NOT_FOUND(HttpStatus.NOT_FOUND),
        USER_NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED),
        USER_NOT_AUTHENTICATED(HttpStatus.FORBIDDEN),
        INTERNAL_COMMUNICATION_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR),
        INTERNAL_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR),
        FILE_NOT_FOUND(HttpStatus.NOT_FOUND),
        IMAGE_METADATA_NOT_FOUND(HttpStatus.NOT_FOUND),
        DEFAULT(HttpStatus.BAD_REQUEST);

        private HttpStatus status;

    }
}

package com.keeper.image.manager.exception;

import com.keeper.image.common.exception.ServiceException;
import lombok.Getter;

/**
 * Custom Exception class dedicated for this service
 */
@Getter
public class ManagerServiceException extends ServiceException {

    public ManagerServiceException(String message) {
        super(message);
    }

    public ManagerServiceException(ErrorType errorType, String message) {
        super(errorType, message);
    }

}

package com.keeper.image.storage.exception;

import com.keeper.image.common.exception.ServiceException;
import lombok.Getter;

/**
 * Custom Exception class dedicated for this service
 */
@Getter
public class StorageServiceException extends ServiceException {

    public StorageServiceException(ErrorType errorType, String message) {
        super(errorType, message);
    }

}

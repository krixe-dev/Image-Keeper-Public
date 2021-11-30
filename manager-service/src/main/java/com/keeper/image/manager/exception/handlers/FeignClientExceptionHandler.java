package com.keeper.image.manager.exception.handlers;

import com.keeper.image.manager.exception.ManagerServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import static com.keeper.image.common.exception.ServiceException.ErrorType.INTERNAL_COMMUNICATION_EXCEPTION;


/**
 * Class for handling exceptions in inner communication between manager-service and storage-service (via Feign)
 * At this stage  only hides all original exception with dedicated ManagerServiceException
 */
@Component
public class FeignClientExceptionHandler implements ErrorDecoder {

    /**
     * Wrap all exception in communication with storage-service into one dedicated exception
     */
    @Override
    public Exception decode(String s, Response response) {
        return new ManagerServiceException(INTERNAL_COMMUNICATION_EXCEPTION, "Error while communicating with STORAGE-SERVICE: " + response.status());
    }
}

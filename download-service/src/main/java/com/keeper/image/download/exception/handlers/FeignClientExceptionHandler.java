package com.keeper.image.download.exception.handlers;

import com.keeper.image.common.exception.ServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

/**
 * Class for handling exceptions in communication between download-service and storage-service (via Feign)
 * At this stage only hides all original exception with dedicated ManagerServiceException
 */
@Component
public class FeignClientExceptionHandler implements ErrorDecoder {

    /**
     * Decode exception and return new instance of ServiceException with custom message
     */
    @Override
    public Exception decode(String s, Response response) {
        return new ServiceException(ServiceException.ErrorType.DEFAULT, "Error while communicating with STORAGE-SERVICE: " + response.status());
    }
}

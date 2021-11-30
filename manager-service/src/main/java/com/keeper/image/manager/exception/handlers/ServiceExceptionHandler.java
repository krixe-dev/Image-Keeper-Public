package com.keeper.image.manager.exception.handlers;

import com.keeper.image.common.exception.handler.CustomExceptionHandler;
import com.keeper.image.manager.exception.ManagerServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 *  Class for handling exceptions for this service.
 *  Its used for mapping custom error code to certain HTTP response status
 */
@ControllerAdvice
public class ServiceExceptionHandler extends CustomExceptionHandler {

    @Value("${eureka.instance.instance-id}")
    private String serviceName;

    /**
     * Create detailed error response with service instance info
     * @param ex handled exception
     * @param request Web request
     * @return Custom response entity with dedicated info
     */
    @ExceptionHandler(ManagerServiceException.class)
    public ResponseEntity<Object> handleImageDataNotFoundException(
            ManagerServiceException ex, WebRequest request) {

        return createResponseForException(ex);
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }
}

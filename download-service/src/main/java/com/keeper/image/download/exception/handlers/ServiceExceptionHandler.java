package com.keeper.image.download.exception.handlers;

import com.keeper.image.common.exception.ServiceException;
import com.keeper.image.common.exception.handler.CustomExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 *  Class for handling exceptions of this REST API service. Its used for mapping custom error code to certain HTTP response status
 */
@ControllerAdvice
public class ServiceExceptionHandler extends CustomExceptionHandler {

    // service instance name
    @Value("${eureka.instance.instance-id}")
    private String serviceName;

    /**
     * Create detailed response entity with instance info
     * @param ex handled exception
     * @param request Web request
     * @return Custom response entity with dedicated info
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleException(
            ServiceException ex, WebRequest request) {

        return createResponseForException(ex);
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

}

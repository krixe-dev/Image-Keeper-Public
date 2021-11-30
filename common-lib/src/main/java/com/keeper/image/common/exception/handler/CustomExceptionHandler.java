package com.keeper.image.common.exception.handler;

import com.keeper.image.common.exception.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 *  Class for handling exceptions thrown by services.
 *  Response message that is constructed can inform user about http error code, message and service instance
 */
public abstract class CustomExceptionHandler extends ResponseEntityExceptionHandler {


    /**
     * Create detailed response entity with instance info
     * @param ex handled exception
     * @return Custom response entity with dedicated info
     */
    public ResponseEntity<Object> createResponseForException(ServiceException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", ex.getErrorType().getStatus());
        body.put("timestamp", LocalDateTime.now());
        body.put("instance", getServiceName());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, ex.getErrorType().getStatus());
    }

    public abstract String getServiceName();

}

package com.keeper.image.manager.service;

import com.keeper.image.common.messages.NewImageMessage;
import com.keeper.image.manager.configuration.RabbitMQConfiguration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Publisher class for rabbitMQ
 */
@Service
public class RabbitMQPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Method used to publish information on rabbitMQ queue
     * Before sending to queue, new instance of <class>NewImageMessage</class> is created
     * @param imageUid unique image identifier
     * @param originalName original image file
     */
    public void sendMessage(String imageUid, String originalName) {
        NewImageMessage newImageMessage = new NewImageMessage(imageUid, originalName);
        rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMAGE_QUEUE_NAME, newImageMessage);
    }


}

package com.keeper.image.storage.service;

import com.keeper.image.common.messages.NewMetadataMessage;
import com.keeper.image.storage.configuration.RabbitMQConfiguration;
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
     * Before sending to queue, new instance of <class>NewMetadataMessage</class> is created
     * @param imageUid unique image identifier
     * @param fileCondition file processing result
     */
    public void sendMessage(String imageUid, Boolean fileCondition) {
        NewMetadataMessage newImageMessage = new NewMetadataMessage(imageUid, fileCondition);
        rabbitTemplate.convertAndSend(RabbitMQConfiguration.METADATA_QUEUE_NAME, newImageMessage);
    }


}

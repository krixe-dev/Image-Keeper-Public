package com.keeper.image.manager.service;

import com.keeper.image.common.messages.NewMetadataMessage;
import com.keeper.image.manager.configuration.RabbitMQConfiguration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * Receiver service class for handling incoming RabbitMQ messages
 */
@Service
public class RabbitMQReceiver {

    private ManagerService managerService;

    private CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    public RabbitMQReceiver(ManagerService managerService) {
        this.managerService = managerService;
    }

    /**
     * Handle message with information about image processing result
     * @param message
     */
    @RabbitListener(queues = RabbitMQConfiguration.METADATA_QUEUE_NAME, containerFactory = "listenerContainerFactory")
    public void receiveMessage(final NewMetadataMessage message) {
        // Image data will be updated after handling it in storage-service
        managerService.updateImageStatus(message.getImageUid(), message.getFileCondition());

        latch.countDown();
    }

}

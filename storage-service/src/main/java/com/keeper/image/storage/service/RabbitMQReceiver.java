package com.keeper.image.storage.service;

import com.keeper.image.common.FileStorageUtil;
import com.keeper.image.common.messages.NewImageMessage;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.storage.configuration.RabbitMQConfiguration;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * Receiver service class for handling incoming RabbitMQ messages
 */
@Service
public class RabbitMQReceiver {

    @Value("${storage.location}")
    private String fileStorageLocation;

    private StorageService storageService;

    private CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    public RabbitMQReceiver(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Handle message with information about new image processing request
     * @param message
     */
    @RabbitListener(queues = RabbitMQConfiguration.IMAGE_QUEUE_NAME, containerFactory = "listenerContainerFactory")
    public void receiveMessage(final NewImageMessage message) {
        // Each file will be processed and proper metadata will be calculated
        ImageMetadataDto imageMetadataDto = FileStorageUtil.handleFile(message.getImageUid(), message.getFileName(), fileStorageLocation);

        // New metadata for each file will be stored in MongoDB
        storageService.addMetadata(imageMetadataDto);

        latch.countDown();
    }

}

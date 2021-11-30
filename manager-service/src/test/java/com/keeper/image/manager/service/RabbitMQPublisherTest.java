package com.keeper.image.manager.service;

import com.keeper.image.common.messages.NewImageMessage;
import com.keeper.image.manager.configuration.RabbitMQConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQPublisher rabbitMQPublisher;

    @BeforeEach
    void setUp() {
        rabbitMQPublisher = new RabbitMQPublisher(rabbitTemplate);
    }

    /**
     * Test if rabbitMQ service handles agrument well and send proper message
     */
    @Test
    void sendMessage_success() {
        // given
        String fileName = RandomStringUtils.randomAlphanumeric(15);
        String imageUid = UUID.randomUUID().toString();

        // when
        rabbitMQPublisher.sendMessage(imageUid, fileName);

        // then
        ArgumentCaptor<String> dataItemArgumentCaptor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NewImageMessage> dataItemArgumentCaptor2= ArgumentCaptor.forClass(NewImageMessage.class);
        verify(rabbitTemplate).convertAndSend(dataItemArgumentCaptor1.capture(), dataItemArgumentCaptor2.capture());
        String queueName = dataItemArgumentCaptor1.getValue();
        NewImageMessage newImageMessage = dataItemArgumentCaptor2.getValue();
        assertThat(queueName).isEqualTo(RabbitMQConfiguration.IMAGE_QUEUE_NAME);
        assertThat(newImageMessage.getImageUid()).isEqualTo(imageUid);
        assertThat(newImageMessage.getFileName()).isEqualTo(fileName);
    }
}
package com.keeper.image.storage.service;

import com.keeper.image.common.messages.NewMetadataMessage;
import com.keeper.image.storage.configuration.RabbitMQConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

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
        // when
        rabbitMQPublisher.sendMessage("A", true);
        // then
        ArgumentCaptor<String> dataItemArgumentCaptor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NewMetadataMessage> dataItemArgumentCaptor2= ArgumentCaptor.forClass(NewMetadataMessage.class);
        verify(rabbitTemplate).convertAndSend(dataItemArgumentCaptor1.capture(), dataItemArgumentCaptor2.capture());
        String argument1 = dataItemArgumentCaptor1.getValue();
        NewMetadataMessage argument2 = dataItemArgumentCaptor2.getValue();
        assertThat(argument1).isEqualTo(RabbitMQConfiguration.METADATA_QUEUE_NAME);
        assertThat(argument2.getImageUid()).isEqualTo("A");
        assertThat(argument2.getFileCondition()).isEqualTo(true);
    }
}
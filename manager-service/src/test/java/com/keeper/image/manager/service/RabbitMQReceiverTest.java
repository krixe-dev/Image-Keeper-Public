package com.keeper.image.manager.service;

import com.keeper.image.common.messages.NewMetadataMessage;
import com.keeper.image.manager.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RabbitMQReceiverTest {

    @Mock
    private ManagerService managerService;

    private RabbitMQReceiver rabbitMQReceiver;

    @BeforeEach
    void setUp() {
        rabbitMQReceiver = new RabbitMQReceiver(managerService);
    }

    /**T
     * est if message receiver handles message and calls service
     */
    @Test
    void receiveMessage_success() {
        // given
        NewMetadataMessage newImageMessage = TestUtil.generateNewMetadataMessage();

        // when
        rabbitMQReceiver.receiveMessage(newImageMessage);

        // then
        verify(managerService, times(1)).updateImageStatus(newImageMessage.getImageUid(), newImageMessage.getFileCondition());
    }
}
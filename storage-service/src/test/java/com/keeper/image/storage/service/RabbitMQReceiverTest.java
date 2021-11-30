package com.keeper.image.storage.service;

import com.keeper.image.common.messages.NewImageMessage;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.storage.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RabbitMQReceiverTest {

    @Mock
    private StorageService storageService;

    private RabbitMQReceiver rabbitMQReceiver;

    @BeforeEach
    void setUp() {
        rabbitMQReceiver = new RabbitMQReceiver(storageService);
    }

    /**T
     * est if message receiver handles message and calls service
     */
    @Test
    void receiveMessage_success() {
        // given
        NewImageMessage newImageMessage = TestUtil.createNewImageMessage();
        // when
        rabbitMQReceiver.receiveMessage(newImageMessage);
        // then
        ArgumentCaptor<ImageMetadataDto> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageMetadataDto.class);
        verify(storageService).addMetadata(dataItemArgumentCaptor.capture());
        ImageMetadataDto imageMetadataDtoAfter = dataItemArgumentCaptor.getValue();
        assertThat(imageMetadataDtoAfter.getImageUid()).isEqualTo(newImageMessage.getImageUid());
        assertThat(imageMetadataDtoAfter.getFileName()).isEqualTo(newImageMessage.getFileName());
    }
}
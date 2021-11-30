package com.keeper.image.storage.service;

import com.keeper.image.common.model.dto.ImageFileDto;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.storage.exception.StorageServiceException;
import com.keeper.image.storage.model.ImageMetadata;
import com.keeper.image.storage.repository.ImageMetadataRepository;
import com.keeper.image.storage.util.TestUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private ImageMetadataRepository imageMetadataRepository;
    @Mock
    private RabbitMQPublisher rabbitMQPublisher;

    private ModelMapper modelMapper = new ModelMapper();

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(imageMetadataRepository, modelMapper, rabbitMQPublisher);
    }

    /**
     * Test if service handles properly adding new metadata
     */
    @Test
    void addMetadata_success() {
        // given
        ImageMetadataDto imageMetadataDto = TestUtil.createImageMetadataDto();
        // when
        storageService.addMetadata(imageMetadataDto);
        // then
        ArgumentCaptor<ImageMetadata> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageMetadata.class);
        verify(imageMetadataRepository).save(dataItemArgumentCaptor.capture());
        ImageMetadata imageMetadata = dataItemArgumentCaptor.getValue();
        assertThat(imageMetadata.getImageUid()).isEqualTo(imageMetadataDto.getImageUid());
        assertThat(imageMetadata.getCreationTime()).isEqualTo(imageMetadataDto.getCreationTime());
        assertThat(imageMetadata.getFileName()).isEqualTo(imageMetadataDto.getFileName());
        assertThat(imageMetadata.getHeight()).isEqualTo(imageMetadataDto.getHeight());
        assertThat(imageMetadata.getWidth()).isEqualTo(imageMetadataDto.getWidth());
        assertThat(imageMetadata.getSha256()).isEqualTo(imageMetadataDto.getSha256());

    }

    /**
     * Test if service calls repository exectly one time
     */
    @Test
    void getMetadata_success() {
        // given
        String uuid = UUID.randomUUID().toString();
        given(imageMetadataRepository.findById(uuid)).willReturn(Optional.of(TestUtil.createImageMetadata()));
        // when
        storageService.getMetadata(uuid);
        // then
        verify(imageMetadataRepository, times(1)).findById(anyString());
    }

    /**
     * Test if service call throw exception StorageServiceException with code IMAGE_METADATA_NOT_FOUND
     */
    @Test
    void getMetadata_IMAGE_METADATA_NOT_FOUND() {
        // given
        String uuid = UUID.randomUUID().toString();
        given(imageMetadataRepository.findById(uuid)).willReturn(Optional.empty());
        // when
        // then
        assertThatThrownBy(() ->  storageService.getMetadata(uuid))
                .isInstanceOf(StorageServiceException.class)
                .hasMessage(String.format("Metadata for image: '%s' doesnt exists.", uuid));
    }


    /**
     * Test if service calls proper repository methods and returns url
     */
    @Test
    void getPublicUrl_success() {
        // given
        String uuid = UUID.randomUUID().toString();
        ImageMetadata imageMetadata = TestUtil.createImageMetadataImageUid(uuid);
        given(imageMetadataRepository.findById(uuid)).willReturn(Optional.of(imageMetadata));
        // when
        String publicUrl = storageService.getPublicUrl(uuid);
        // then
        // check if service calls reposotory and saved data
        verify(imageMetadataRepository, times(1)).save(imageMetadata);
        // check if service returns url that is not empty
        assertThat(publicUrl).isNotBlank();
    }

    /**
     * Test if service calls proper repository and imageSecureUrl has correct values
     */
    @Test
    void getPublicUrl_success_imageSeruceUrlNotEmpty() {
        // given
        String uuid = UUID.randomUUID().toString();
        ImageMetadata imageMetadata = TestUtil.createImageMetadataImageUid(uuid);
        given(imageMetadataRepository.findById(uuid)).willReturn(Optional.of(imageMetadata));
        // when
        String publicUrl = storageService.getPublicUrl(uuid);
        // then
        ArgumentCaptor<ImageMetadata> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageMetadata.class);
        verify(imageMetadataRepository).save(dataItemArgumentCaptor.capture());
        ImageMetadata imageMetadataAfetrService = dataItemArgumentCaptor.getValue();
        // check if imagesecureurl has proper values
        assertThat(imageMetadataAfetrService.getImageSecureUrl().getSecureUrl()).isNotBlank();
        assertThat(imageMetadataAfetrService.getImageSecureUrl().getExpirationDate()).isAfter(new Date());
    }

    /**
     * Test if service calls proper repository and publicUrl has correct values
     */
    @Test
    void getPublicUrl_success_imageSeruceUrlCorrect() {
        // given
        String uuid = UUID.randomUUID().toString();
        ImageMetadata imageMetadata = TestUtil.createImageMetadataImageUid(uuid);
        given(imageMetadataRepository.findById(uuid)).willReturn(Optional.of(imageMetadata));
        // when
        String publicUrl = storageService.getPublicUrl(uuid);
        // then
        ArgumentCaptor<ImageMetadata> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageMetadata.class);
        verify(imageMetadataRepository).save(dataItemArgumentCaptor.capture());
        ImageMetadata imageMetadataAfetrService = dataItemArgumentCaptor.getValue();
        // check if imagesecureurl has proper values
        assertThat(publicUrl).contains(imageMetadataAfetrService.getImageSecureUrl().getSecureUrl());
    }

    /**
     * Test if service returns correct DTO object
     */
    @Test
    void getFileInfoBySecureId_success() {
        // given
        String secureId = RandomStringUtils.randomAlphanumeric(40);
        ImageMetadata imageMetadata = TestUtil.createImageMetadataSecureId(secureId);
        imageMetadata.getImageSecureUrl().setExpirationDate(DateUtils.addMinutes(new Date(), 1));
        given(imageMetadataRepository.findByImageSecureUrl(secureId)).willReturn(Optional.of(imageMetadata));
        // when
        ImageFileDto imageFileDto = storageService.getFileInfoBySecureId(secureId);
        // then
        // check if service return DTO object with correct values
        assertThat(imageFileDto.getImageUid()).isEqualTo(imageMetadata.getImageUid());
        assertThat(imageFileDto.getOriginalName()).isEqualTo(imageMetadata.getFileName());

    }

    /**
     * Check if service handles the StorageServiceException with code IMAGE_METADATA_NOT_FOUND
     */
    @Test
    void getFileInfoBySecureId_IMAGE_METADATA_NOT_FOUND() {
        // given
        String secureUrl = RandomStringUtils.randomAlphanumeric(40);
        given(imageMetadataRepository.findByImageSecureUrl(secureUrl)).willReturn(Optional.empty());
        // when
        // then
        assertThatThrownBy(() ->  storageService.getFileInfoBySecureId(secureUrl))
                .isInstanceOf(StorageServiceException.class)
                .hasMessage(String.format("Metadata for secureUrl: '%s' doesnt exists.", secureUrl));

    }

    /**
     * Check if service handles the StorageServiceException with code IMAGE_METADATA_NOT_FOUND after checking the expiration date
     */
    @Test
    void getFileInfoBySecureId_IMAGE_METADATA_NOT_FOUND_afterExpiration() {
        // given
        String secureUrl = RandomStringUtils.randomAlphanumeric(40);
        ImageMetadata imageMetadata = TestUtil.createImageMetadataSecureId(secureUrl);
        imageMetadata.getImageSecureUrl().setExpirationDate(DateUtils.addMinutes(new Date(), -1));
        given(imageMetadataRepository.findByImageSecureUrl(secureUrl)).willReturn(Optional.of(imageMetadata));
        // when
        // then
        assertThatThrownBy(() ->  storageService.getFileInfoBySecureId(secureUrl))
                .isInstanceOf(StorageServiceException.class)
                .hasMessage(String.format("SecureId: '%s' has expired.", secureUrl));

    }
}
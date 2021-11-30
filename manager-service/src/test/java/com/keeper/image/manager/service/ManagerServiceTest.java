package com.keeper.image.manager.service;

import com.keeper.image.common.FileStorageUtil;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.manager.client.CacheClient;
import com.keeper.image.manager.client.StorageServiceClient;
import com.keeper.image.manager.exception.ManagerServiceException;
import com.keeper.image.manager.model.ImageData;
import com.keeper.image.manager.model.UserData;
import com.keeper.image.manager.model.dto.InputImageDataDto;
import com.keeper.image.manager.model.enums.ImageStatus;
import com.keeper.image.manager.repository.ImageDataReposotory;
import com.keeper.image.manager.repository.UserDataRepository;
import com.keeper.image.manager.security.AuthWrapper;
import com.keeper.image.manager.security.UserRole;
import com.keeper.image.manager.util.TestUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ImageDataReposotory imageDataReposotory;
    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private RabbitMQPublisher rabbitMQPublisher;
    @Mock
    private CacheClient cacheClient;
    @Mock
    private StorageServiceClient storageServiceClient;

    private ModelMapper modelMapper = new ModelMapper();

    private ManagerService managerService;

    @BeforeAll
    void prepare() {
        mockStatic(FileStorageUtil.class);
    }

    @BeforeEach
    void setUp() {
        managerService = new ManagerService(imageDataReposotory, userDataRepository, modelMapper, storageServiceClient, rabbitMQPublisher, cacheClient);
    }

    /**
     * Test if normal user is getting information about his images
     */
    @Test
    void getAllData_forNormalUser() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();

        // when
        managerService.getAllData(authWrapper);

        // then
        // Normal system user should get only information about his images (another repository method is called for admin)
        verify(imageDataReposotory, times(1)).findAllByUserName(anyString());
    }

    /**
     * Test if admin user is getting information about all images
     */
    @Test
    void getAllData_forAdmin() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper("Marian", UserRole.ADMIN);

        // when
        managerService.getAllData(authWrapper);

        // then
        // Admin system user should get information about all images
        verify(imageDataReposotory, times(1)).findAll();
    }

    /**
     * Test if admin user (with additional role USER) is getting information about all images
     */
    @Test
    void getAllData_forAdminWithUserRole() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper("Zbigniew", UserRole.ADMIN, UserRole.USER);

        // when
        managerService.getAllData(authWrapper);

        // then
        // Admin system user should get information about all images
        verify(imageDataReposotory, times(1)).findAll();
    }

    /**
     * Test if user has access to image details. No communication with storage-service.
     */
    @Test
    void getImageDataByImageId_successForImageInStatusQUEUED() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // when
        managerService.getImageDataByImageId(imageData.getImageUid(), authWrapper);

        // then
        verify(imageDataReposotory, times(1)).findByImageUid(anyString());
    }


    /**
     * Test user gets image details. With communication with storage-service. No Cache entry
     */
    @Test
    void getImageDataByImageId_successForImageInStatusPRESENTNoCache() {
        // GIVEN
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        ImageMetadataDto imageMetadataDto = TestUtil.generateImageMetadataDto();
        imageData.setStatus(ImageStatus.PRESENT);
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));
            // Metadata cache does not has entry for this image
        given(cacheClient.get(imageData.getImageUid())).willReturn(null);
            // a communication with storage-service will be called
        given(storageServiceClient.getImageMetadata(imageData.getImageUid())).willReturn(imageMetadataDto);

        // WHEN
        managerService.getImageDataByImageId(imageData.getImageUid(), authWrapper);

        // THEN
        verify(imageDataReposotory, times(1)).findByImageUid(anyString());
        verify(storageServiceClient, times(1)).getImageMetadata(anyString());
    }

    /**
     * Test user gets image details. Bo communication with storage-service. With Cache entry
     */
    @Test
    void getImageDataByImageId_successForImageInStatusPRESENTWithCache() {
        // GIVEN
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        ImageMetadataDto imageMetadataDto = TestUtil.generateImageMetadataDto();
        imageData.setStatus(ImageStatus.PRESENT);
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));
        // Metadata cache does not has entry for this image
        given(cacheClient.get(imageData.getImageUid())).willReturn(imageMetadataDto);
        // a communication with storage-service will not be called

        // WHEN
        managerService.getImageDataByImageId(imageData.getImageUid(), authWrapper);

        // THEN
        verify(imageDataReposotory, times(1)).findByImageUid(anyString());
        verify(storageServiceClient, times(0)).getImageMetadata(anyString());
    }

    /**
     * Test user is not authorized to access image details.
     */
    @Test
    void getImageDataByImageId_ExceptionForUserNotAuthorized() {
        // GIVEN
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser("Roman");
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        // THEN
        assertThatThrownBy(() ->  managerService.getImageDataByImageId(imageData.getImageUid(), authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Unauthorized access to image data with uid: '%s' by user with name: '%s'",
                        imageData.getImageUid(), authWrapper.getUserName()));
    }

    /**
     * Test user trying to access non existing image details.
     */
    @Test
    void getImageDataByImageId_ExceptionForNoImageFOund() {
        // GIVEN
        String imageUid = UUID.randomUUID().toString();
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        given(imageDataReposotory.findByImageUid(imageUid)).willReturn(Optional.empty());

        // WHEN
        // THEN
        assertThatThrownBy(() ->  managerService.getImageDataByImageId(imageUid, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Image with id: '%s' does not exist", imageUid));
    }

    /**
     * Test that image status is changed during delete operation
     */
    @Test
    void deleteImageData_success() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // when
        managerService.deleteImageData(imageData.getImageUid(), authWrapper);

        // then
        // check if status of image is changed and passed to repository with save operation
        ArgumentCaptor<ImageData> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageData.class);
        verify(imageDataReposotory).save(dataItemArgumentCaptor.capture());
        ImageData imageDataWithNewStatus = dataItemArgumentCaptor.getValue();
        assertThat(imageDataWithNewStatus.getImageUid()).isEqualTo(imageData.getImageUid());
        assertThat(imageDataWithNewStatus.getStatus()).isEqualTo(ImageStatus.DELETED);
    }

    /**
     * Test that operation fails because user can not access this image
     */
    @Test
    void deleteImageData_UserNotAuthorized() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser("Alicja");
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.getImageDataByImageId(imageData.getImageUid(), authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Unauthorized access to image data with uid: '%s' by user with name: '%s'",
                        imageData.getImageUid(), authWrapper.getUserName()));
    }

    /**
     * Test that operation fails because image not exist
     */
    @Test
    void deleteImageData_ImageDataNotExist() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        String imageUid = UUID.randomUUID().toString();
        given(imageDataReposotory.findByImageUid(imageUid)).willReturn(Optional.empty());

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.getImageDataByImageId(imageUid, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Image with id: '%s' does not exist", imageUid));
    }

    /**
     * Test that image data can be updated (image in status PRESENT)
     */
    @Test
    void changeImageData_success() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        imageData.setStatus(ImageStatus.PRESENT);
        InputImageDataDto inputImageDataDto = TestUtil.generateInputImageDataDto();
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));
        given(imageDataReposotory.save(imageData)).willReturn(imageData);

        // when
        managerService.changeImageData(imageData.getImageUid(), inputImageDataDto, authWrapper);

        // then
        // check if new data was copied into ImageData and saved
        ArgumentCaptor<ImageData> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageData.class);
        verify(imageDataReposotory).save(dataItemArgumentCaptor.capture());
        ImageData imageDataWithNewDetails = dataItemArgumentCaptor.getValue();
        assertThat(imageDataWithNewDetails.getImageUid()).isEqualTo(imageData.getImageUid());
        assertThat(imageDataWithNewDetails.getTitle()).isEqualTo(inputImageDataDto.getTitle());
        assertThat(imageDataWithNewDetails.getDescription()).isEqualTo(inputImageDataDto.getDescription());
    }

    /**
     * Test that image data can't be updated (image in status QUEUED)
     */
    @Test
    void changeImageData_errorInvalidImageStatus() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        InputImageDataDto inputImageDataDto = TestUtil.generateInputImageDataDto();
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.changeImageData(imageData.getImageUid(), inputImageDataDto, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Only image with PRESENT status can be changed. Image uid: '%s', status: [%s]", imageData.getImageUid(), imageData.getStatus()));
    }

    /**
     * Test that image data can't be updated (image not exist)
     */
    @Test
    void changeImageData_errorNoImageFound() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        String imageUid = UUID.randomUUID().toString();
        InputImageDataDto inputImageDataDto = TestUtil.generateInputImageDataDto();
        given(imageDataReposotory.findByImageUid(imageUid)).willReturn(Optional.empty());

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.changeImageData(imageUid, inputImageDataDto, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Image with id: '%s' does not exist", imageUid));
    }

    /**
     * Test that image data can't be updated (image belong to another user)
     */
    @Test
    void changeImageData_errorUserNotAuthorized() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser("Karolina");
        InputImageDataDto inputImageDataDto = TestUtil.generateInputImageDataDto();
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.changeImageData(imageData.getImageUid(), inputImageDataDto, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Unauthorized access to image data with uid: '%s' by user with name: '%s'",
                        imageData.getImageUid(), authWrapper.getUserName()));
    }

    /**
     * Test the actualization of image status to PRESENT
     */
    @Test
    void updateImageStatus_successNewStatusPresent() {
        // given
        AuthWrapper authWrapper = null;
        ImageData imageData = TestUtil.generateNewImageData();
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        managerService.updateImageStatus(imageData.getImageUid(), true);

        // THEN
        // check if new data was copied into ImageData and saved
        ArgumentCaptor<ImageData> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageData.class);
        verify(imageDataReposotory).save(dataItemArgumentCaptor.capture());
        ImageData imageDataWithNewDetails = dataItemArgumentCaptor.getValue();
        assertThat(imageDataWithNewDetails.getImageUid()).isEqualTo(imageData.getImageUid());
        assertThat(imageDataWithNewDetails.getStatus()).isEqualTo(ImageStatus.PRESENT);
        // check if instance of object was removed from cache
        verify(cacheClient, times(1)).remove(imageData.getImageUid());
    }

    /**
     * Test the actualization of image status to CORRUPTED
     */
    @Test
    void updateImageStatus_successNewStatusCorrupted() {
        // given
        ImageData imageData = TestUtil.generateNewImageData();
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        managerService.updateImageStatus(imageData.getImageUid(), false);

        // THEN
        // check if new data was copied into ImageData and saved
        ArgumentCaptor<ImageData> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageData.class);
        verify(imageDataReposotory).save(dataItemArgumentCaptor.capture());
        ImageData imageDataWithNewDetails = dataItemArgumentCaptor.getValue();
        assertThat(imageDataWithNewDetails.getImageUid()).isEqualTo(imageData.getImageUid());
        assertThat(imageDataWithNewDetails.getStatus()).isEqualTo(ImageStatus.CORRUPTED);
        // check if instance of object was removed from cache
        verify(cacheClient, times(1)).remove(imageData.getImageUid());
    }

    /**
     * Test the actualization of image status and exception thrown when image not exists
     */
    @Test
    void updateImageStatus_errorImageNotExist() {
        // given
        String imageUid = UUID.randomUUID().toString();
        given(imageDataReposotory.findByImageUid(imageUid)).willReturn(Optional.empty());

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.updateImageStatus(imageUid, true))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Image with id: '%s' does not exist", imageUid));
    }

    /**
     * Test method for returning image file by image uid
     */
    @Test
    void getFileByImageUid_success() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        imageData.setStatus(ImageStatus.PRESENT);
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));
//        mockStatic(FileStorageUtil.class);
        when(FileStorageUtil.loadFileAsResource(imageData.getImageUid(), null)).thenReturn(null);

        // when
        ResourceWrapper resourceWrapper = managerService.getFileByImageUid(imageData.getImageUid(), authWrapper);
        // then
        // check if resource wrapper contains proper data and original filename
        assertThat(resourceWrapper).isNotNull();
        assertThat(resourceWrapper.getOriginalFileName()).isEqualTo(imageData.getOriginalFileName());
    }

    /**
     * Test method for returning image file by image uid, check exception throw if image was in wrong status (QUEUED)
     */
    @Test
    void getFileByImageUid_errorImageInWrongStatus() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.getFileByImageUid(imageData.getImageUid(), authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Image with id: '%s' in wrong status for download. [%s]", imageData.getImageUid(), imageData.getStatus()));
    }

    /**
     * Test method for returning image file by image uid, check exception throw if image was not found
     */
    @Test
    void getFileByImageUid_errorImageNotFound() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        String imageUid = UUID.randomUUID().toString();
        given(imageDataReposotory.findByImageUid(imageUid)).willReturn(Optional.empty());

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.getFileByImageUid(imageUid, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("Image with id: '%s' does not exist", imageUid));
    }

    /**
     * Test if secure url is generated
     */
    @Test
    void generatePublicUrlToImage_success() {
        // given
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        String secureUrl = RandomStringUtils.randomAlphanumeric(15);
        ImageData imageData = TestUtil.generateNewImageDataWithUser(authWrapper.getUserName());
        given(imageDataReposotory.findByImageUid(imageData.getImageUid())).willReturn(Optional.of(imageData));
        given(storageServiceClient.getPublicUrlForFile(imageData.getImageUid())).willReturn(secureUrl);

        // when
        managerService.generatePublicUrlToImage(imageData.getImageUid(), authWrapper);
        // check that storage-service client was called
        verify(storageServiceClient, times(1)).getPublicUrlForFile(imageData.getImageUid());
    }

    /**
     * Test if new image is registered in system
     */
    @Test
    void addImage_success() {
        // GIVEN
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        UserData userData = TestUtil.generateNewUserData(authWrapper.getUserName());
        String originalFileName =  RandomStringUtils.randomAlphanumeric(20);
        // make sure that user was in system
        given(userDataRepository.findUserDataByName(userData.getName())).willReturn(Optional.of(userData));
        // mock file stroing in file-system
//        mockStatic(FileStorageUtil.class);
        when(FileStorageUtil.storeFile(any(), any(),any())).thenReturn(originalFileName);

        // WHEN
        managerService.addImage(null, authWrapper);

        // THEN
        // check if new image is saved in DB with proper values
        ArgumentCaptor<ImageData> dataItemArgumentCaptor = ArgumentCaptor.forClass(ImageData.class);
        verify(imageDataReposotory).save(dataItemArgumentCaptor.capture());
        ImageData newImageData = dataItemArgumentCaptor.getValue();
        assertThat(newImageData.getStatus()).isEqualTo(ImageStatus.QUEUED);
        assertThat(newImageData.getOriginalFileName()).isEqualTo(originalFileName);

        // check if message was added to rabbitmq queue
        verify(rabbitMQPublisher, times(1)).sendMessage(anyString(), anyString());
    }

    /**
     * Test if while new image is registered in system, exception is thrown - USER_NOT_FOUND
     */
    @Test
    void addImage_errorUSER_NOT_FOUND() {
        // GIVEN
        AuthWrapper authWrapper = TestUtil.generateAuthWrapper();
        // make sure that user was in system
        given(userDataRepository.findUserDataByName(authWrapper.getUserName())).willReturn(Optional.empty());

        // WHEN
        // THEN
        // check if proper exception is thrown
        assertThatThrownBy(() ->  managerService.addImage(null, authWrapper))
                .isInstanceOf(ManagerServiceException.class)
                .hasMessage(String.format("User with name: '%s' does not exist", authWrapper.getUserName()));
    }
}
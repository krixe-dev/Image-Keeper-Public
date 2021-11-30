package com.keeper.image.manager.service;

import com.keeper.image.common.FileStorageUtil;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.manager.client.CacheClient;
import com.keeper.image.manager.client.StorageServiceClient;
import com.keeper.image.manager.exception.ManagerServiceException;
import com.keeper.image.manager.model.ImageData;
import com.keeper.image.manager.model.UserData;
import com.keeper.image.manager.model.dto.ImageDataDto;
import com.keeper.image.manager.model.dto.InputImageDataDto;
import com.keeper.image.manager.model.enums.ImageStatus;
import com.keeper.image.manager.repository.ImageDataReposotory;
import com.keeper.image.manager.repository.UserDataRepository;
import com.keeper.image.manager.security.AuthWrapper;
import com.keeper.image.manager.security.UserRole;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.keeper.image.common.exception.ServiceException.ErrorType.*;

/**
 * Service class for handling system operations on image data
 */
@Service
public class ManagerService {
    private Logger logger = LoggerFactory.getLogger(ManagerService.class);

    @Value("${eureka.instance.instance-id}")
    private String instanceName;

    @Value("${storage.location}")
    private String fileStorageLocation;

    @Autowired
    public ManagerService(ImageDataReposotory imageDataReposotory,
                          UserDataRepository userDataRepository,
                          ModelMapper modelMapper,
                          StorageServiceClient storageServiceClient,
                          RabbitMQPublisher publisher,
                          CacheClient cacheClient) {
        this.imageDataReposotory = imageDataReposotory;
        this.userDataRepository = userDataRepository;
        this.modelMapper = modelMapper;
        this.storageServiceClient = storageServiceClient;
        this.publisher = publisher;
        this.cacheClient = cacheClient;
    }

    private ImageDataReposotory imageDataReposotory;
    private UserDataRepository userDataRepository;
    private ModelMapper modelMapper;
    private StorageServiceClient storageServiceClient;
    private RabbitMQPublisher publisher;
    private CacheClient cacheClient;


    /**
     * Return information about all images from system (that can be viewed by authenticated user)
     * @param authWrapper authenticated user information
     * @return list of DTO objects containing information about images in system
     */
    public List<ImageDataDto> getAllData(AuthWrapper authWrapper) {
        logger.info(":: getAllData()");

        List<ImageData> imageDataList;
        if(authWrapper.checkRole(UserRole.ADMIN)) {
            imageDataList = imageDataReposotory.findAll();
        } else {
            imageDataList = imageDataReposotory.findAllByUserName(authWrapper.getUserName());
        }

        logger.debug("imageDataList data size="+imageDataList.size());

        // TODO zmienic na pojedyncze odpytanie z lista
        List<ImageDataDto> collectionOfImageDTOs = imageDataList.stream()
                .map(i -> modelMapper.map(i, ImageDataDto.class))
                .collect(Collectors.toList());
        logger.debug("Collection mapped to List of ImageDataDto");

        collectionOfImageDTOs.forEach(i -> {
            handleMetadataForImage(i.getImageUid(), i);
        });

        return collectionOfImageDTOs;
    }

    /**
     * Get image data for specific image identifier
     * @param imageId image unique identifier
     * @param authWrapper authentication data wrapper with user information
     * @return ImageDataDto DTO object for controller
     * @throws ManagerServiceException
     */
    public ImageDataDto getImageDataByImageId(String imageId, AuthWrapper authWrapper) throws ManagerServiceException {
        // Check if image data is present (handle basic validation and throw default exceptions)
        ImageData imageData = getImageDataAndCheckPrivileges(imageId, authWrapper.getUserName());

        ImageDataDto imageDataDto = modelMapper.map(imageData, ImageDataDto.class);

        // Calling storage-service for image metadata
        handleMetadataForImage(imageId, imageDataDto);

        return imageDataDto;
    }

    /**
     * Delete image data (chage status to DELETED)
     * @param imageUid image unique identifier
     * @param authWrapper authentication data wrapper with user information
     */
    public void deleteImageData(String imageUid, AuthWrapper authWrapper) {
        logger.info(":: deleteImageData(imageUid={}, authWrapper={})", imageUid, authWrapper);
        // Check if image data is present (handle basic validation and throw default exceptions)
        ImageData imageData = getImageDataAndCheckPrivileges(imageUid, authWrapper.getUserName());

        // Update image status and data
        imageData.setStatus(ImageStatus.DELETED);
        imageData.setLastUpdateTime(new Date());
        logger.debug("Image status updated to="+ ImageStatus.DELETED);

        // TODO dodac obsluge powiadomienia storage-service o usunieciu obrazka

        // save changes in DB
        imageDataReposotory.save(imageData);
        logger.debug("Image data saved to DB");
    }

    /**
     * Change image data (update description and title)
     * @param imageUid image unique identifier
     * @param inputImageDataDto new title and description for image data
     * @param authWrapper authentication data wrapper with user information
     * @return image data DTO after updating
     */
    public ImageDataDto changeImageData(String imageUid, InputImageDataDto inputImageDataDto, AuthWrapper authWrapper) {
        logger.info(":: changeImageData(imageUid={}, inputImageMetadataDto, authWrapper={})", imageUid, authWrapper);
        // Check if image data is present (handle basic validation and throw default exceptions)
        ImageData imageData = getImageDataAndCheckPrivileges(imageUid, authWrapper.getUserName());

        // Check status of image data
        if(!ImageStatus.PRESENT.equals(imageData.getStatus())) {
            String errorMessage = String.format("Only image with PRESENT status can be changed. Image uid: '%s', status: [%s]", imageUid, imageData.getStatus());
            logger.warn(errorMessage);
            throw new ManagerServiceException(IMAGE_STATUS_INVALID, errorMessage);
        }

        // Update image data
        imageData.setTitle(inputImageDataDto.getTitle());
        imageData.setDescription(inputImageDataDto.getDescription());
        imageData.setLastUpdateTime(new Date());
        logger.debug("Image title and description updated");

        // save changes in DB
        imageData = imageDataReposotory.save(imageData);
        logger.debug("Image data saved to DB");

        // convert entity to DTO object
        return modelMapper.map(imageData, ImageDataDto.class);
    }

    /**
     * Update image data status after processing in Storage-Service
     * @param imageUid image unique identifier
     * @param fileCondition file processing result.
     *        (true if file processed succesfully, false otherwise)
     */
    public void updateImageStatus(String imageUid, Boolean fileCondition) {
        logger.info(":: updateImageStatus(imageUid={}, fileCondition={})", imageUid, fileCondition);
        // Check if image data is present (handle basic validation and throw default exceptions)
        ImageData imageData = getImageDataAndCheckPrivileges(imageUid, null);

        // Image after handling in storage-service is either correct or damaged
        if(fileCondition) {
            //File that was processed without errors is flaged as PRESET and ready to use
            imageData.setStatus(ImageStatus.PRESENT);
        } else {
            //File that was processed with errors is flaged as CORRUPTED, no other actions is made
            // at this point
            imageData.setStatus(ImageStatus.CORRUPTED);
        }
        logger.debug("Image status changed to="+ imageData.getStatus());

        // Remove image metadata from cache so it can be reloaded in next attempt
        cacheClient.remove(imageUid);
        logger.debug("Image metadata for image was removed from cache(imageUid={})", imageUid);

        // Save entity in DB
        imageDataReposotory.save(imageData);
        logger.debug("Image data saved to DB");
    }

    /**
     * Get image file (as Resource object)
     * @param imageUid image unique identifier
     * @param authWrapper authentication data wrapper with user information
     * @return image Resource and original file name
     * @throws ManagerServiceException
     */
    public ResourceWrapper getFileByImageUid(String imageUid, AuthWrapper authWrapper) throws ManagerServiceException {
        logger.info(":: getFileByImageUid(imageUid={}, authWrapper={})", imageUid, authWrapper);
        // Check if image data is present (handle basic validation and throw default exceptions)
        ImageData imageData = getImageDataAndCheckPrivileges(imageUid, authWrapper.getUserName());

        // Image info can be accessed only for images in status PRESENT
        if(!ImageStatus.PRESENT.equals(imageData.getStatus())) {
            String errorMessage = String.format("Image with id: '%s' in wrong status for download. [%s]", imageUid, imageData.getStatus());
            logger.warn(errorMessage);
            throw new ManagerServiceException(IMAGE_STATUS_INVALID, errorMessage);
        }
        logger.debug("Image status is {}", imageData.getStatus());

        Resource resource = FileStorageUtil.loadFileAsResource(imageUid, fileStorageLocation);
        return new ResourceWrapper(resource, imageData.getOriginalFileName());
    }

    /**
     * Generate a public uri to image
     * @param imageUid unique image identifier
     * @param authWrapper authentication data wrapper with user information
     * @return public url to image
     * @throws ManagerServiceException
     */
    public String generatePublicUrlToImage(String imageUid, AuthWrapper authWrapper) throws ManagerServiceException {
        logger.info(":: generatePublicUrlToImage(imageUid={}, authWrapper={})", imageUid, authWrapper);
        // Check if image data is present (handle basic validation and throw default exceptions)
        getImageDataAndCheckPrivileges(imageUid, authWrapper.getUserName());

        return storageServiceClient.getPublicUrlForFile(imageUid);
    }

    /**
     * Add new image to the system by handling additional operations
     * - Create and save image data in database.
     * - Save file in file system.
     * - Send information to Storage-Service
     * @param file image file
     * @param authWrapper data wrapper with information about authenticated user
     * @return image data DTO after applying new values
     */
    @Transactional
    public ImageDataDto addImage(MultipartFile file, AuthWrapper authWrapper)  {
        logger.info(":: addImage(file, authWrapper={})", authWrapper);
        // Check if user data is present (handle basic validation and throw default exceptions)
        UserData userData = getUserData(authWrapper.getUserName());

        // Create database entry in ImageData table with initial data for this iamge
        ImageData imageData = createNewImageData(userData);
        logger.debug("New imageData structure created");

        // Store image file (unprocessed) in file storage
        String originalFileName = FileStorageUtil.storeFile(file, imageData.getImageUid(), fileStorageLocation);
        logger.debug("file stored in file system");

        imageData.setOriginalFileName(originalFileName);

        // Persist new entity
        imageDataReposotory.save(imageData);
        logger.debug("Image data saved in DB");

        // Send message to queue to inform about new file waiting to process
        publisher.sendMessage(imageData.getImageUid(), originalFileName);
        logger.debug("Message sent to queue");

        // convert entity to DTO object
        return modelMapper.map(imageData, ImageDataDto.class);
    }

    /**
     * Find image data and check if authenticated user is allowed to get this data (is its owner)
     * @param imageUid unique image identifier
     * @param userName authenticated user name
     * @return ImageData object
     */
    private ImageData getImageDataAndCheckPrivileges(String imageUid, String userName) {
        logger.info(":: getImageDataAndCheckPrivileges(imageUid={}, userName={})", imageUid, userName);
        Optional<ImageData> imageOptional = imageDataReposotory.findByImageUid(imageUid);
        if (!imageOptional.isPresent()) {
            String errorMessage = String.format("Image with id: '%s' does not exist", imageUid);
            logger.error(errorMessage);
            throw new ManagerServiceException(IMAGE_NOT_FOUND, errorMessage);
        }

        ImageData imageData = imageOptional.get();

        if(userName != null) {
            if (!imageData.getUser().getName().equals(userName)) {
                String errorMessage = String.format("Unauthorized access to image data with uid: '%s' by user with name: '%s'",
                        imageUid, userName);
                logger.error(errorMessage);
                throw new ManagerServiceException(USER_NOT_AUTHORIZED, errorMessage);
            }
        }

        logger.info(":: image status and privileges is OK");
        return imageData;
    }

    /**
     * Find user data and handle validation (user exists)
     * @param userName user name
     * @return UserData object
     */
    private UserData getUserData(String userName) {
        logger.info(":: getUserData(userName={})", userName);
        Optional<UserData> userOptional = userDataRepository.findUserDataByName(userName);
        if(!userOptional.isPresent()) {
            String errorMessage = String.format("User with name: '%s' does not exist", userName);
            logger.error(errorMessage);
            throw new ManagerServiceException(USER_NOT_FOUND, errorMessage);
        }

        return userOptional.get();
    }

    /**
     * Get additional image metadata from storage-service
     * @param imageId image unique identifier
     * @param imageDataDto image data DTO
     */
    private void handleMetadataForImage(String imageId, ImageDataDto imageDataDto) {
        logger.info(":: handleMetadataForImage(imageUid={}, imageDataDto)", imageId);

        imageDataDto.setInstance(instanceName);
        ImageStatus imageStatus = ImageStatus.valueOf(imageDataDto.getStatus());

        logger.debug("Image status is=" + imageStatus);
        if(ImageStatus.PRESENT.equals(imageStatus)) {
            // Read image metadata from Cache
            ImageMetadataDto imageMetadataDto = cacheClient.get(imageId);
            if(imageMetadataDto != null) {
                logger.debug("Image metadata was found in cache");
            } else {
                logger.debug("Image metadata will be obtained from storage-service");
                imageMetadataDto = storageServiceClient.getImageMetadata(imageId);
                // store image metadatata in cache
                cacheClient.put(imageId, imageMetadataDto);
            }
            imageDataDto.setWidth(imageMetadataDto.getWidth());
            imageDataDto.setHeight(imageMetadataDto.getHeight());
            imageDataDto.setFileUrl("/images/"+ imageId +"/file");
            imageDataDto.setHash(imageMetadataDto.getSha256());
        }
    }

    /**
     * Create template of ImageData object
     * @param userData user data to link with new ImageData
     * @return ImageData object
     */
    private ImageData createNewImageData(UserData userData) {
        ImageData imageData = new ImageData();
        imageData.setUser(userData);
        imageData.setImageUid(UUID.randomUUID().toString());
        imageData.setCreationTime(new Date());
        imageData.setLastUpdateTime(new Date());
        imageData.setStatus(ImageStatus.QUEUED);

        return imageData;
    }

}

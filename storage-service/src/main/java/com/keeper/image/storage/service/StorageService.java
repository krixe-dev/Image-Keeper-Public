package com.keeper.image.storage.service;

import com.keeper.image.common.exception.ServiceException;
import com.keeper.image.common.model.dto.ImageFileDto;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.storage.exception.StorageServiceException;
import com.keeper.image.storage.model.ImageMetadata;
import com.keeper.image.storage.model.ImageSecureUrl;
import com.keeper.image.storage.repository.ImageMetadataRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * Service class for handling system operations on image files and image metadata
 */
@Service
public class StorageService {
    public static final String HTTP_LOCALHOST_9010_DOWNLOAD = "http://localhost:9010/download/";
    private Logger logger = LoggerFactory.getLogger(StorageService.class);

    private ImageMetadataRepository imageMetadataRepository;

    private ModelMapper modelMapper;

    private RabbitMQPublisher rabbitMQPublisher;

    @Autowired
    public StorageService(ImageMetadataRepository imageDataRepository, ModelMapper modelMapper, RabbitMQPublisher rabbitMQPublisher) {
        this.imageMetadataRepository = imageDataRepository;
        this.modelMapper = modelMapper;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    /**
     * Add new metadata information for processed image file
     * @param imageMetadataDto
     */
    public void addMetadata(ImageMetadataDto imageMetadataDto) {
        logger.info(":: addMetadata(imageMetadataDto.uid={})", imageMetadataDto.getImageUid());
        ImageMetadata imageMetadata = modelMapper.map(imageMetadataDto, ImageMetadata.class);
        imageMetadataRepository.save(imageMetadata);

        logger.debug("Send message to queue after adding metadata to DB");
        rabbitMQPublisher.sendMessage(imageMetadata.getImageUid(), imageMetadataDto.getFileCondition());
    }

    /**
     * Get image metadata for specific image
     * @param imageUid image unique identifier
     * @return ImageMetadataDto object containing metadata information
     */
    public ImageMetadataDto getMetadata(String imageUid) {
        logger.info(":: getMetadata(imageUid={})", imageUid);
        Optional<ImageMetadata> imageMetadataOptional = imageMetadataRepository.findById(imageUid);
        if(!imageMetadataOptional.isPresent()) {
            throw new StorageServiceException(ServiceException.ErrorType.IMAGE_METADATA_NOT_FOUND,
                    String.format("Metadata for image: '%s' doesnt exists.", imageUid));
        }

        ImageMetadataDto imageMetadataDto = modelMapper.map(imageMetadataOptional.get(), ImageMetadataDto.class);
        return imageMetadataDto;
    }

    /**
     * Generate secure public url for accessing file
     * @param imageUid image unique identifier
     * @return secure url for downloading this file
     */
    public String getPublicUrl(String imageUid) {
        logger.info(":: getPublicUrl(imageUid={})", imageUid);
        Optional<ImageMetadata> imageMetadataOptional = imageMetadataRepository.findById(imageUid);

        ImageSecureUrl imageSecureUrl = new ImageSecureUrl();
        imageSecureUrl.setSecureUrl(RandomStringUtils.randomAlphanumeric(64));
        imageSecureUrl.setExpirationDate(DateUtils.addMinutes(new Date(), 2));
        ImageMetadata imageMetadata = imageMetadataOptional.get();
        imageMetadata.setImageSecureUrl(imageSecureUrl);

        imageMetadataRepository.save(imageMetadata);

        String secureUrl = HTTP_LOCALHOST_9010_DOWNLOAD +imageSecureUrl.getSecureUrl();
        logger.info(":: getPublicUrl: "+secureUrl);
        return secureUrl;
    }

    /**
     * Get file info requested by secure url
     * @param secureUrl secure url for downloading
     * @return ImageFileDto object containing file info
     */
    public ImageFileDto getFileInfoBySecureId(String secureUrl) {
        logger.info(":: getFileInfoBySecureId(secureUrl={})", secureUrl);
        Optional<ImageMetadata> imageMetadataOptional = imageMetadataRepository.findByImageSecureUrl(secureUrl);
        if(!imageMetadataOptional.isPresent()) {
            throw new StorageServiceException(ServiceException.ErrorType.IMAGE_METADATA_NOT_FOUND,
                    String.format("Metadata for secureUrl: '%s' doesnt exists.", secureUrl));
        }

        ImageMetadata imageMetadata = imageMetadataOptional.get();
        if(imageMetadata.getImageSecureUrl().getExpirationDate().before(new Date())) {
            throw new StorageServiceException(StorageServiceException.ErrorType.IMAGE_METADATA_NOT_FOUND,
                    String.format("SecureId: '%s' has expired.", secureUrl));
        }

        ImageFileDto imageFileDto = new ImageFileDto(imageMetadata.getImageUid(), imageMetadata.getFileName());

        logger.info(":: getFileInfoBySecureId.ImageUid: "+imageFileDto.getImageUid());
        return imageFileDto;
    }

}

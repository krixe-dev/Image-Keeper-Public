package com.keeper.image.storage.controller;

import com.keeper.image.common.model.dto.ImageFileDto;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for REST API of this service
 */
@RestController
@RequestMapping(path = "/storage")
public class StorageServiceController {

    Logger logger = LoggerFactory.getLogger(StorageServiceController.class);

    private StorageService storageService;

    @Autowired
    public StorageServiceController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Return image metadata informations
     * @param imageUid unique image identifier
     * @return image metadata DTO
     */
    @GetMapping(path = "/metadata/{imageUid}")
    public ImageMetadataDto getMetadata(@PathVariable("imageUid") String imageUid) {
        logger.info("-> getMetadata(imageUid={})", imageUid);
        ImageMetadataDto imageMetadataDto = storageService.getMetadata(imageUid);

        logger.info("<- getMetadata");
        return imageMetadataDto;
    }

    /**
     * Return public secure url for downloading file
     * @param imageId unique image identifier
     * @return file url
     */
    @GetMapping(path = "/files/{imageId}/url")
    public String getPublicUrlForFile(@PathVariable("imageId") String imageId) {
        logger.info("-> getPublicUrlForFile(imageId={})", imageId);

        String publicUrl = storageService.getPublicUrl(imageId);

        logger.info("<- getPublicUrlForFile: "+publicUrl);
        return publicUrl;
    }

    /**
     * Return image file informations
     * @param secureId secure url for accessing image file
     * @return image file DTO
     */
    @GetMapping(path = "/files/{secureId}")
    public ImageFileDto getFileInfoBySecureId(@PathVariable("secureId") String secureId) {
        logger.info("-> getFileIdBySecureId(secureId={})", secureId);
        ImageFileDto imageFileDto = storageService.getFileInfoBySecureId(secureId);

        logger.info("<- getFileIdBySecureId: "+imageFileDto.getImageUid());
        return imageFileDto;
    }

}

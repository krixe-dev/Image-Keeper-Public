package com.keeper.image.manager.client;

import com.keeper.image.common.model.dto.ImageMetadataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Client class for handling request to storage-service
 */
@FeignClient(name = "storage-service")
@RequestMapping("/storage")
public interface StorageServiceClient {

    /**
     * Get image metadata information from storage-service
     * @param imageUid unique image identifier
     * @return image metadata DTO
     */
    @GetMapping("/metadata/{imageId}")
    ImageMetadataDto getImageMetadata(@PathVariable("imageId") String imageUid);

    /**
     * Get public secure url for image file
     * @param imageId unique image identifier
     * @return public url to image
     */
    @GetMapping("/files/{imageId}/url")
    String getPublicUrlForFile(@PathVariable("imageId") String imageId);

}

package com.keeper.image.download.client;

import com.keeper.image.common.model.dto.ImageFileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Client class for handling communication with storage-service
 */
@FeignClient(name = "storage-service")
@RequestMapping("/storage")
public interface StorageServiceClient {

    /**
     * Get file information from storage-service
     * @param secureId temporary secure identifier of image
     * @return image file DTO object
     */
    @GetMapping("/files/{secureId}")
    ImageFileDto getFileIdBySecureId(@PathVariable("secureId") String secureId);

}

package com.keeper.image.download.controller;

import com.keeper.image.download.service.DownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class with REST API's
 */
@RestController
@RequestMapping(path = "/download")
public class DownloadServiceController {

    Logger logger = LoggerFactory.getLogger(DownloadServiceController.class);

    private DownloadService downloadService;

    @Autowired
    public DownloadServiceController(DownloadService managerService) {
        this.downloadService = managerService;
    }

    /**
     * Get image file as Resource object
     * @param secureUrl random and temporary image identifier used for downloading
     * @return image file
     */
    @GetMapping(path = "{secureUrl}")
    public ResponseEntity<Resource> getImageFile(@PathVariable("secureUrl") String secureUrl) {
        logger.info("-> getImageFile(secureUrl={})", secureUrl);
        String contentType = "application/octet-stream";

        ResourceWrapper resourceWrapper = downloadService.getFileBySecureId(secureUrl);

        ResponseEntity<Resource> body = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceWrapper.getOriginalFileName() + "\"")
                .body(resourceWrapper.getResource());

        logger.info("<- getImageFile, originalImageFile={}", resourceWrapper.getOriginalFileName());
        return body;
    }
}

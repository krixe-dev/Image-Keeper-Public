package com.keeper.image.manager.controller;

import com.keeper.image.manager.exception.ManagerServiceException;
import com.keeper.image.manager.model.dto.ImageDataDto;
import com.keeper.image.manager.model.dto.InputImageDataDto;
import com.keeper.image.manager.security.AuthWrapper;
import com.keeper.image.manager.security.JwtUtil;
import com.keeper.image.manager.security.UserRole;
import com.keeper.image.manager.service.ManagerService;
import com.keeper.image.manager.service.ResourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller class for REST API of this service
 */
@RestController
@RequestMapping(path = "/images")
public class ManagerServiceController {

    Logger logger = LoggerFactory.getLogger(ManagerServiceController.class);

    private ManagerService managerService;
    private JwtUtil jwtUtil;

    @Autowired
    public ManagerServiceController(ManagerService managerService, JwtUtil jwtUtil) {
        this.managerService = managerService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Return all image data
     * @return list of all images that can be viewed by authenticated user
     */
    @GetMapping
    public List<ImageDataDto> getData() {
        logger.info("-> getData()");

        AuthWrapper authWrapper = getAuthWrapper();
        List<ImageDataDto> allData = managerService.getAllData(authWrapper);

        logger.info("<- getData");
        return allData;
    }

    /**
     * Return image data for specific image
     * @param imageUid unique image identifier
     * @return image data DTO
     * @throws ManagerServiceException
     */
    @GetMapping(path = "{imageUid}")
    public ImageDataDto getImageData(@PathVariable("imageUid") String imageUid) throws ManagerServiceException {
        logger.info("-> getImageData(imageUid={})", imageUid);

        AuthWrapper authWrapper = getAuthWrapper();
        ImageDataDto imageDto = managerService.getImageDataByImageId(imageUid, authWrapper);

        logger.info("<- getImageData");
        return imageDto;
    }

    /**
     * Return secure url that can be used to download image file by unauthenticated user
     * @param imageUid unique image identifier
     * @return secure url to image
     * @throws ManagerServiceException
     */
    @GetMapping(path = "{imageUid}/url")
    public ResponseEntity<String> getPublicImageUrl(@PathVariable("imageUid") String imageUid) throws ManagerServiceException {
        logger.info("-> getPublicImageUrl(imageUid={})", imageUid);

        AuthWrapper authWrapper = getAuthWrapper();
        String url = managerService.generatePublicUrlToImage(imageUid, authWrapper);

        ResponseEntity<String> body = ResponseEntity.ok().body(url);

        logger.info("<- getPublicImageUrl, url={}", url);
        return body;
    }

    /**
     * Return image file
     * @param imageUid unique image identifier
     * @return resource with image file
     * @throws ManagerServiceException
     */
    @GetMapping(path = "{imageUid}/file")
    public ResponseEntity<Resource> getImageFile(@PathVariable("imageUid") String imageUid) throws ManagerServiceException {
        logger.info("-> getImageFile(imageUid={})", imageUid);
        String contentType = "application/octet-stream";

        AuthWrapper authWrapper = getAuthWrapper();
        ResourceWrapper resourceWrapper = managerService.getFileByImageUid(imageUid, authWrapper);

        ResponseEntity<Resource> body = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resourceWrapper.getOriginalFileName() + "\"")
                .body(resourceWrapper.getResource());

        logger.info("<- getImageFile, originalImageFile={}", resourceWrapper.getOriginalFileName());
        return body;
    }

    /**
     * Upload new image to system
     * @param file image file to be stored in system
     * @return image data DTO object
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImageDataDto addImage(@RequestParam("file") MultipartFile file) {
        logger.info("-> addImage()");

        AuthWrapper authWrapper = getAuthWrapper();
        ImageDataDto imageDataDto = managerService.addImage(file, authWrapper);

        logger.info("<- addImage, imageUid={}", imageDataDto.getImageUid());
        return imageDataDto;
    }

    /**
     * Update image data (title, description)
     * @param inputImageDataDto request body with image details (title, description)
     * @param imageUid unique image identifier
     * @return image data DTO object with updated information
     */
    @PutMapping(path = "{imageUid}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ImageDataDto updateImageInfo(@Valid  @RequestBody InputImageDataDto inputImageDataDto,
                                        @PathVariable("imageUid") String imageUid) {
        logger.info("-> updateImageInfo(imageUid={})", imageUid);

        AuthWrapper authWrapper = getAuthWrapper();
        ImageDataDto imageDataDto = managerService.changeImageData(imageUid, inputImageDataDto, authWrapper);

        logger.info("<- updateImageInfo");
        return imageDataDto;
    }

    /**
     * Delete image data from system
     * @param imageUid unique image identifier
     */
    @DeleteMapping(path = "{imageUid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImageData(@PathVariable("imageUid") String imageUid) {
        logger.info("-> deleteImageData(imageUid={})", imageUid);

        AuthWrapper authWrapper = getAuthWrapper();
        managerService.deleteImageData(imageUid, authWrapper);
        logger.info("<- deleteImageData");
    }

    /**
     * Return security information about authenticated user
     * @return AuthWrapper object describing authenticated user
     */
    private AuthWrapper getAuthWrapper() {
        logger.info(":: getAuthWrapper");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String principal = jwtUtil.readUserName(authentication);
        List<UserRole> userRolesList = jwtUtil.readUserRoles(authentication);

        AuthWrapper authWrapper = new AuthWrapper(principal, userRolesList);

        logger.info("User principal: "+principal);
        logger.info("Role ADMIN: "+authWrapper.checkRole(UserRole.ADMIN));
        logger.info("Role USER: "+authWrapper.checkRole(UserRole.USER));

        return authWrapper;
    }

}

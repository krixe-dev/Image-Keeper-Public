package com.keeper.image.common;

import com.keeper.image.common.exception.ServiceException;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

import static com.keeper.image.common.exception.ServiceException.ErrorType.HANDLE_FILE_EXCPETION;

/**
 * Util class for handling image read/write operations on the file system
 */
public class FileStorageUtil {

    /**
     * Handle processing new image in the system. Image metadata will be extracted from image file
     * @param imageUid unique image identifier (file stored on file system will be named afeter this identifier)
     * @param originalFileName original file name
     * @param fileStorageLocation location on file system where files should be stored
     * @return ImageMetadataDto object containing image metadata
     */
    public static ImageMetadataDto handleFile(String imageUid, String originalFileName, String fileStorageLocation) {
        try {
            // Get path to file
            Path targetLocation = getFileStoragePath(fileStorageLocation).resolve(imageUid);

            ImageMetadataDto imageMetadataDto = ImageMetadataDto.builder()
                    .creationTime(new Date())
                    .imageUid(imageUid)
                    .fileName(originalFileName)
                    .height(new Random().nextInt(800)) // TODO extract dimensions
                    .width(new Random().nextInt(800)) // TODO extract dimensions
                    .sha256(checksum(targetLocation.toString(), MessageDigest.getInstance("SHA-256")))
                    .fileCondition(true)
                    .build();

            return imageMetadataDto;
        } catch (Exception ex) {
            ImageMetadataDto imageMetadataDto = ImageMetadataDto.builder()
                    .imageUid(imageUid)
                    .fileName(originalFileName)
                    .fileCondition(false)
                    .build();
            return imageMetadataDto;
        }
    }

    /**
     * Store new file in file system
     * @param file image file
     * @param imageUid unique image identifier (file stored on file system will be named afeter this identifier)
     * @param fileStorageLocation location on file system where files should be stored
     * @return original file name to be used later by manager-service
     */
    public static String storeFile(MultipartFile file, String imageUid, String fileStorageLocation) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = getFileStoragePath(fileStorageLocation).resolve(imageUid);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (Exception ex) {
            throw new ServiceException(HANDLE_FILE_EXCPETION, "Could not save file in file system " + imageUid);
        }
    }

    /**
     * Read file from file system as Resource
     * @param imageUid unique image identifier and also the file name on file system
     * @param fileStorageLocation location on file system where files are stored
     * @return Resource object with file data
     */
    public static Resource loadFileAsResource(String imageUid, String fileStorageLocation) {
        try {
            Path filePath = getFileStoragePath(fileStorageLocation).resolve(imageUid).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new ServiceException(HANDLE_FILE_EXCPETION, "File not found " + imageUid);
            }
        } catch (MalformedURLException ex) {
            throw new ServiceException(HANDLE_FILE_EXCPETION, "File not found " + imageUid);
        }
    }

    private static Path getFileStoragePath(String fileStorageLocation) {
        Path fileStorageLocationPath = null;
        
        if(fileStorageLocationPath == null) {
            fileStorageLocationPath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocationPath)) {
                try {
                    Files.createDirectories(fileStorageLocationPath);
                } catch (Exception ex) {
                    throw new ServiceException(HANDLE_FILE_EXCPETION, "Could not create directory for uploaded files");
                }
            }
        }

        return fileStorageLocationPath;
    }

    private static String checksum(String filePath, MessageDigest md) throws IOException {
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath), md)) {
            while (dis.read() != -1);
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

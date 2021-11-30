package com.keeper.image.manager.util;

import com.keeper.image.common.messages.NewMetadataMessage;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.manager.model.ImageData;
import com.keeper.image.manager.model.UserData;
import com.keeper.image.manager.model.dto.ImageDataDto;
import com.keeper.image.manager.model.dto.InputImageDataDto;
import com.keeper.image.manager.model.dto.UserDataDto;
import com.keeper.image.manager.model.enums.ImageStatus;
import com.keeper.image.manager.security.AuthWrapper;
import com.keeper.image.manager.security.UserRole;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class TestUtil {

    public static ImageData generateNewImageData(UserData userData) {
        ImageData imageData = generateNewImageData();
        imageData.setUser(userData);

        return imageData;
    }

    public static ImageData generateNewImageData(UserData userData, String imageUid) {
        ImageData imageData = generateNewImageData();
        imageData.setUser(userData);
        imageData.setImageUid(imageUid);

        return imageData;
    }

    public static AuthWrapper generateAuthWrapper() {
        return generateAuthWrapper(RandomStringUtils.randomAlphanumeric(15));
    }

    public static AuthWrapper generateAuthWrapper(String userName) {
        AuthWrapper authWrapper = new AuthWrapper();
        authWrapper.setUserName(userName);
        authWrapper.setUserRolesList(Arrays.asList(UserRole.USER));

        return authWrapper;
    }

    public static InputImageDataDto generateInputImageDataDto() {
        return InputImageDataDto.builder()
                .title(RandomStringUtils.randomAlphanumeric(10))
                .description(RandomStringUtils.randomAlphanumeric(100))
                .build();
    }

    public static AuthWrapper generateAuthWrapper(String userName, UserRole... roles) {
        AuthWrapper authWrapper = new AuthWrapper();
        authWrapper.setUserName(userName);
        authWrapper.setUserRolesList(Arrays.asList(roles));

        return authWrapper;
    }

    public static ImageData generateNewImageDataWithUser(String userName) {
        UserData userData = generateNewUserData(userName);
        return generateNewImageData(userData);
    }

    public static ImageData generateNewImageData() {
        ImageData imageData = new ImageData();
        imageData.setOriginalFileName(RandomStringUtils.randomAlphanumeric(17));
        imageData.setStatus(ImageStatus.QUEUED);
        imageData.setLastUpdateTime(new Date());
        imageData.setCreationTime(new Date());
        imageData.setDescription(RandomStringUtils.randomAlphanumeric(100));
        imageData.setTitle(RandomStringUtils.randomAlphanumeric(100));
        imageData.setImageUid(UUID.randomUUID().toString());

        return imageData;
    }

    public static ImageDataDto generateNewImageDataDto() {
        ImageDataDto imageDataDto = new ImageDataDto();
        imageDataDto.setFileUrl(RandomStringUtils.randomAlphanumeric(17));
        imageDataDto.setStatus(ImageStatus.QUEUED.name());
        imageDataDto.setLastUpdateTime(new Date());
        imageDataDto.setCreationTime(new Date());
        imageDataDto.setDescription(RandomStringUtils.randomAlphanumeric(100));
        imageDataDto.setTitle(RandomStringUtils.randomAlphanumeric(100));
        imageDataDto.setImageUid(UUID.randomUUID().toString());

        return imageDataDto;
    }

    public static UserData generateNewUserData(String userName) {
        UserData userData = generateNewUserData();
        userData.setName(userName);

        return userData;
    }

    public static UserData generateNewUserData() {
        UserData userData = new UserData();
        userData.setName(RandomStringUtils.randomAlphanumeric(20));

        return userData;
    }

    public static UserDataDto generateNewUserDataDto(String userName) {
        UserDataDto userDataDto = new UserDataDto();
        userDataDto.setName(userName);

        return userDataDto;
    }

    public static ImageMetadataDto generateImageMetadataDto() {
        return ImageMetadataDto.builder()
                .imageUid(UUID.randomUUID().toString())
                .creationTime(new Date())
                .fileCondition(true)
                .height(RandomUtils.nextInt(600))
                .width(RandomUtils.nextInt(600))
                .fileName(RandomStringUtils.randomAlphanumeric(15))
                .sha256(RandomStringUtils.randomAlphanumeric(32))
                .build();
    }

    public static NewMetadataMessage generateNewMetadataMessage() {
        return NewMetadataMessage.builder()
                .imageUid(UUID.randomUUID().toString())
                .fileCondition(true)
                .build();
    }
}

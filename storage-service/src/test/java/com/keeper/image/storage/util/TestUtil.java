package com.keeper.image.storage.util;

import com.keeper.image.common.messages.NewImageMessage;
import com.keeper.image.common.model.dto.ImageMetadataDto;
import com.keeper.image.storage.model.ImageMetadata;
import com.keeper.image.storage.model.ImageSecureUrl;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Date;
import java.util.UUID;

public class TestUtil {

    public static ImageMetadata createImageMetadataSecureId(String secureId) {
        ImageMetadata imageMetadata = createImageMetadata();
        ImageSecureUrl imageSecureUrl = new ImageSecureUrl();
        imageSecureUrl.setExpirationDate(new Date());
        if(secureId == null) {
            imageSecureUrl.setSecureUrl(RandomStringUtils.randomAlphanumeric(40));
        } else {
            imageSecureUrl.setSecureUrl(secureId);
        }
        imageMetadata.setImageSecureUrl(imageSecureUrl);

        return imageMetadata;
    }

    public static ImageMetadata createImageMetadataImageUid(String imageUid) {
        ImageMetadata imageMetadata = createImageMetadata();
        imageMetadata.setImageUid(imageUid);

        return imageMetadata;
    }

    public static ImageMetadata createImageMetadata() {
        ImageMetadata imageMetadata = new ImageMetadata();
        imageMetadata.setImageUid(UUID.randomUUID().toString());
        imageMetadata.setFileName(RandomStringUtils.randomAlphanumeric(15));
        imageMetadata.setWidth(RandomUtils.nextInt(600));
        imageMetadata.setHeight(RandomUtils.nextInt(600));
        imageMetadata.setCreationTime(new Date());
        imageMetadata.setSha256(RandomStringUtils.randomAlphanumeric(32));

        return imageMetadata;
    }

    public static ImageMetadataDto createImageMetadataDto() {
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

    public static NewImageMessage createNewImageMessage() {
        return NewImageMessage.builder()
                .imageUid(UUID.randomUUID().toString())
                .fileName(RandomStringUtils.randomAlphanumeric(15))
                .build();
    }
}

package com.keeper.image.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO class for handling communication between services
 * It stores metadata describing image:
 * - unique image identifier
 * - creation date
 * - image dimensions
 * - file hash (SHA-256)
 * - file condition
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageMetadataDto implements Serializable {

    @JsonProperty("imageId")
    private String imageUid;
    @JsonProperty("createdOn")
    private Date creationTime;
    @JsonProperty("image-height")
    private Integer height;
    @JsonProperty("image-width")
    private Integer width;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("sha256")
    private String sha256;
    @JsonProperty("fileCondition")
    private Boolean fileCondition;
}

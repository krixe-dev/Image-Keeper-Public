package com.keeper.image.manager.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * The DTO class used to exchange information to and from the service
 * This class stores information describing image
 */
@Getter
@Setter
@ToString
public class ImageDataDto {

    @JsonProperty("imageId")
    private String imageUid;
    @JsonProperty("status")
    private String status;
    @JsonProperty("owner")
    private UserDataDto user;
    @JsonProperty("createdOn")
    private Date creationTime;
    @JsonProperty("updatedOn")
    private Date lastUpdateTime;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("image-width")
    private Integer width;
    @JsonProperty("image-height")
    private Integer height;
    @JsonProperty("hash")
    private String hash;
    @JsonProperty("fileUrl")
    private String fileUrl;
    @JsonProperty("instance")
    private String instance;

}

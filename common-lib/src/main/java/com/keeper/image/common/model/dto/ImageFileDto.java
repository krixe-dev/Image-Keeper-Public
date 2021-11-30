package com.keeper.image.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO class for handling communication between services
 * It stores data describing image file:
 * - unique image identifier
 * - original file name
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageFileDto {

    @JsonProperty("imageUid")
    private String imageUid;

    @JsonProperty("originalName")
    private String originalName;

}

package com.keeper.image.storage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Document class for storing information about image metadata
 */
@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageMetadata {

    @Id
    private String imageUid;

    @NotBlank
    private String fileName;

    @NotNull
    private Integer width;

    @NotNull
    private Integer height;

    @NotNull
    private Date creationTime;

    @NotNull
    private String sha256;

    private ImageSecureUrl imageSecureUrl;
}

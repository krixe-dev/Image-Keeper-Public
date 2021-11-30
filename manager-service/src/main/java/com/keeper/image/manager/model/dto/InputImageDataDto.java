package com.keeper.image.manager.model.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * The DTO class used to exchange information to and from the service
 * This class stores information describing image, used only in PUT operation
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class InputImageDataDto {
    @NotBlank(message = "Image title is required")
    private String title;
    private String description;

}

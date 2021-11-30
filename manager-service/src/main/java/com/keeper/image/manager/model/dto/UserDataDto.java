package com.keeper.image.manager.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The DTO class used to exchange information to and from the service
 * This class stores information describing user
 */
@Getter
@Setter
public class UserDataDto {

    @JsonProperty("userName")
    private String name;
}

package com.keeper.image.storage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Class for storing information about secure url created for image
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageSecureUrl {

    @Id
    private String secureUrl;

    @NotNull
    private Date expirationDate;
}

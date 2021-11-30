package com.keeper.image.manager.model;

import com.keeper.image.manager.model.dto.ImageDataDto;
import com.keeper.image.manager.model.dto.UserDataDto;
import com.keeper.image.manager.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for ModelMapper mechanism and conversion from/to DTO objects
 */
public class ModelMapperTest {

    private ModelMapper modelMapper = new ModelMapper();

    @Test
    public void testModelMapperUserDataToDto() {
        // given
        UserData userData =TestUtil.generateNewUserData();
        // when
        UserDataDto userDto = modelMapper.map(userData, UserDataDto.class);
        // then
        assertThat(userDto.getName()).isEqualTo(userData.getName());
    }

    @Test
    public void testModelMapperUserDtoToEntity() {
        // given
        UserDataDto userDto = TestUtil.generateNewUserDataDto("Adam");
        // when
        UserData userData = modelMapper.map(userDto, UserData.class);
        // then
        assertThat(userData.getName()).isEqualTo(userDto.getName());
    }

    @Test
    public void testModelMapperImageDataToDto() {
        // given
        UserData userData = TestUtil.generateNewUserData();
        ImageData imageData = TestUtil.generateNewImageData(userData);
        // when
        ImageDataDto imageDto = modelMapper.map(imageData, ImageDataDto.class);
        // then
        assertThat(imageDto.getTitle()).isEqualTo(imageData.getTitle());
        assertThat(imageDto.getDescription()).isEqualTo(imageData.getDescription());
        assertThat(imageDto.getCreationTime()).isEqualTo(imageData.getCreationTime());
        assertThat(imageDto.getLastUpdateTime()).isEqualTo(imageData.getLastUpdateTime());
        assertThat(imageDto.getStatus()).isEqualTo(imageData.getStatus().name());
        assertThat(imageDto.getUser().getName()).isEqualTo(imageData.getUser().getName());
        assertThat(imageDto.getImageUid()).isEqualTo(imageData.getImageUid());
    }

    @Test
    public void testModelMapperImageDtoToEntity() {
        // given
        ImageDataDto imageDataDto = TestUtil.generateNewImageDataDto();
        // when
        ImageData imageData = modelMapper.map(imageDataDto, ImageData.class);
        // then
        assertThat(imageData.getCreationTime()).isEqualTo(imageDataDto.getCreationTime());
        assertThat(imageData.getLastUpdateTime()).isEqualTo(imageDataDto.getLastUpdateTime());
        assertThat(imageData.getDescription()).isEqualTo(imageDataDto.getDescription());
        assertThat(imageData.getTitle()).isEqualTo(imageDataDto.getTitle());
        assertThat(imageData.getImageUid()).isEqualTo(imageDataDto.getImageUid());
        assertThat(imageData.getStatus().name()).isEqualTo(imageDataDto.getStatus());
    }

}

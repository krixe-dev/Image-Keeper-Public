package com.keeper.image.manager.repository;

import com.keeper.image.manager.model.ImageData;
import com.keeper.image.manager.model.UserData;
import com.keeper.image.manager.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageDataReposotoryTest {

    @Autowired
    private ImageDataReposotory imageDataReposotory;

    @Autowired
    private UserDataRepository userDataRepository;

    private UserData userData;

    @BeforeAll
    void prepareUserData() {
        UserData userData = TestUtil.generateNewUserData("janusz");
        this.userData = userDataRepository.save(userData);
    }

    @AfterEach
    void cleanUp() {
        // Clean after each test
        imageDataReposotory.deleteAll();
    }


    /**
     * Test findAllByUserName SUCCESS
     */
    @Test
    void findAllByUserName_success() {
        // given
        String imageUid = UUID.randomUUID().toString();
        imageDataReposotory.save(TestUtil.generateNewImageData(userData, imageUid));
        // when
        List<ImageData> dataList = imageDataReposotory.findAllByUserName("janusz");
        // then
        assertThat(dataList).isNotEmpty();
    }

    /**
     * Test findAllByUserName nothing was found for this uid
     */
    @Test
    void findAllByUserName_nothingFound() {
        // given
        String imageUid = UUID.randomUUID().toString();
        imageDataReposotory.save(TestUtil.generateNewImageData(userData, imageUid));
        // when
        List<ImageData> dataList = imageDataReposotory.findAllByUserName("adam");
        // then
        assertThat(dataList).isEmpty();
    }

    /**
     * Test findByImageUid SUCCESS
     */
    @Test
    void findByImageUid_success() {
        // given
        String imageUid = UUID.randomUUID().toString();
        imageDataReposotory.save(TestUtil.generateNewImageData(userData, imageUid));
        // when
        boolean exists = imageDataReposotory.findByImageUid(imageUid).isPresent();
        // then
        assertThat(exists).isTrue();
    }

    /**
     * Test findByImageUid SUCCESS nothing was found for this uid
     */
    @Test
    void findByImageUid_nothingFound() {
        // given
        String imageUid = UUID.randomUUID().toString();
        // when
        boolean exists = imageDataReposotory.findByImageUid(imageUid).isPresent();
        // then
        assertThat(exists).isFalse();

    }

    /**
     * Test findAllImages SUCCESS
     */
    @Test
    void findAllImages_success() {
        // given
        String imageUid = UUID.randomUUID().toString();
        String imageUid2 = UUID.randomUUID().toString();
        imageDataReposotory.save(TestUtil.generateNewImageData(userData, imageUid));
        imageDataReposotory.save(TestUtil.generateNewImageData(userData, imageUid2));
        // when
        List<ImageData> dataList = imageDataReposotory.findAll();
        // then
        assertThat(dataList.size()).isEqualTo(2);
    }
}
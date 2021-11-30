package com.keeper.image.storage.repository;

import com.keeper.image.storage.model.ImageMetadata;
import com.keeper.image.storage.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataMongoTest
class ImageMetadataRepositoryTest {

    @Autowired
    ImageMetadataRepository imageMetadataRepository;


    @AfterEach
    void clearData() {
        imageMetadataRepository.deleteAll();
    }

    /**
     * Test imageMetadataRepository.findById SUCCESS
     */
    @Test
    void findByImageId_success() {
        //given
        ImageMetadata imageMetadata = TestUtil.createImageMetadata();
        imageMetadataRepository.save(imageMetadata);
        // when
        boolean exists = imageMetadataRepository.findById(imageMetadata.getImageUid()).isPresent();
        // then
        assertThat(exists).isTrue();
    }

    /**
     * Test imageMetadataRepository.findById FAILURE
     */
    @Test
    void findByImageId_failure() {
        // when
        boolean exists = imageMetadataRepository.findById(UUID.randomUUID().toString()).isPresent();
        // then
        assertThat(exists).isFalse();
    }

    /**
     * Test imageMetadataRepository.findAll SUCCESS
     */
    @Test
    void findAll_success() {
        //given
        imageMetadataRepository.save(TestUtil.createImageMetadata());
        imageMetadataRepository.save(TestUtil.createImageMetadata());
        imageMetadataRepository.save(TestUtil.createImageMetadata());
        // when
        List<ImageMetadata> listOfMetadata = imageMetadataRepository.findAll();
        // then
        assertThat(listOfMetadata).size().isEqualTo(3);
    }

    /**
     * Test imageMetadataRepository.findBySecureId SUCCESS
     */
    @Test
    void findBySecureId_success() {
        //given
        ImageMetadata imageMetadata = TestUtil.createImageMetadataSecureId(null);
        imageMetadataRepository.save(imageMetadata);
        // when
        boolean exists = imageMetadataRepository.findByImageSecureUrl(imageMetadata.getImageSecureUrl().getSecureUrl()).isPresent();
        // then
        assertThat(exists).isTrue();
    }

    /**
     * Test imageMetadataRepository.findBySecureId FAILURE
     */
    @Test
    void findBySecureId_failure() {
        // when
        boolean exists = imageMetadataRepository.findByImageSecureUrl("AKJDNHSNFHSNFSFU").isPresent();
        // then
        assertThat(exists).isFalse();
    }

}
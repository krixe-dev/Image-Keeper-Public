package com.keeper.image.storage.repository;

import com.keeper.image.storage.model.ImageMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * Repository class for handling DB operations on ImageMetadata documents
 */
public interface ImageMetadataRepository extends MongoRepository<ImageMetadata, String> {

    /**
     * Find image metadata by secure url
     * @param secureUrl secure url for image
     * @return ImageMetadata describing image file
     */
    @Query(value = "{ 'imageSecureUrl.secureUrl' : ?0}")
    Optional<ImageMetadata> findByImageSecureUrl(String secureUrl);

}

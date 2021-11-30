package com.keeper.image.manager.repository;

import com.keeper.image.manager.model.ImageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for handling DB operations on ImageData entities
 */
@Repository
public interface ImageDataReposotory extends JpaRepository<ImageData, Long> {

    /**
     * Find image information by user name
     * @param name user name
     * @return List of ImageData
     */
    @Query("SELECT i FROM ImageData i WHERE i.user.name = ?1")
    List<ImageData> findAllByUserName(String name);

    /**
     * Find image information by unique image id
     * @param imageUid unique image identifier
     * @return Optional of ImageData
     */
    Optional<ImageData> findByImageUid(String imageUid);
}

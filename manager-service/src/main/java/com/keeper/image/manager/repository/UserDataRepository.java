package com.keeper.image.manager.repository;

import com.keeper.image.manager.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository class for handling DB operations on UserData entities
 */
@Repository
public interface UserDataRepository  extends JpaRepository<UserData, Long> {

    /**
     * Find users by name
     * @param name user name
     * @return Optional of UserData
     */
    Optional<UserData> findUserDataByName(String name);
}

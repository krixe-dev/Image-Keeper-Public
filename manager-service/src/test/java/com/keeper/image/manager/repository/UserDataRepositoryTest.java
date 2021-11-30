package com.keeper.image.manager.repository;

import com.keeper.image.manager.util.TestUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDataRepositoryTest {

    @Autowired
    private UserDataRepository userDataRepository;

    @AfterEach
    void cleanUp() {
        // Clean after each test
        userDataRepository.deleteAll();
    }

    /**
     * Test findUserDataByName SUCCESS
     */
    @Test
    void findUserDataByName_success() {
        // given
        String userName = RandomStringUtils.randomAlphanumeric(20);
        userDataRepository.save(TestUtil.generateNewUserData(userName));
        // when
        boolean exists = userDataRepository.findUserDataByName(userName).isPresent();
        // then
        assertThat(exists).isTrue();
    }

    /**
     * Test findUserDataByName nothing was found for this user name
     */
    @Test
    void findUserDataByName_nothingFound() {
        String userName = RandomStringUtils.randomAlphanumeric(20);
        // when
        boolean exists = userDataRepository.findUserDataByName(userName).isPresent();
        // then
        assertThat(exists).isFalse();
    }
}
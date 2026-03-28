package com.coursebuddy.repository;

import com.coursebuddy.domain.po.UserSettingsPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettingsPO, Long> {

    Optional<UserSettingsPO> findByUserId(Long userId);
}

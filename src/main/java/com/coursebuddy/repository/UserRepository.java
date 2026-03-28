package com.coursebuddy.repository;

import com.coursebuddy.domain.po.UserPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserPO, Long> {

    Optional<UserPO> findByUsername(String username);

    Optional<UserPO> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserPO u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.realName LIKE %:keyword%")
    Page<UserPO> searchByKeyword(String keyword, Pageable pageable);
}

package com.coursebuddy.repository;

import com.coursebuddy.domain.po.TokenPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenPO, Long> {

    Optional<TokenPO> findByRefreshToken(String refreshToken);

    List<TokenPO> findByUserId(Long userId);

    void deleteByUserIdAndIsRevokedTrue(Long userId);
}

package com.coursebuddy.repository;

import com.coursebuddy.domain.po.AiUsageStatsPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AiUsageStatsRepository extends JpaRepository<AiUsageStatsPO, Long> {

    Page<AiUsageStatsPO> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.totalTokens), 0) FROM AiUsageStatsPO s WHERE s.userId = :userId")
    Long sumTotalTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) FROM AiUsageStatsPO s WHERE s.userId = :userId AND s.status = 'SUCCESS'")
    long countSuccessByUserId(@Param("userId") Long userId);
}

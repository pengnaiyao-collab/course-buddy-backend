package com.coursebuddy.repository;

import com.coursebuddy.domain.po.NoteCategoryPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteCategoryRepository extends JpaRepository<NoteCategoryPO, Long> {

    List<NoteCategoryPO> findByUserIdOrderBySortOrderAsc(Long userId);

    Optional<NoteCategoryPO> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}

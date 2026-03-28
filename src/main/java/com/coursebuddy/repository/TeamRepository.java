package com.coursebuddy.repository;

import com.coursebuddy.domain.po.TeamPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<TeamPO, Long> {

    Page<TeamPO> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT t FROM TeamPO t JOIN TeamMemberPO tm ON t.id = tm.teamId WHERE tm.userId = :userId")
    Page<TeamPO> findByMemberId(Long userId, Pageable pageable);

    Page<TeamPO> findByCourseId(Long courseId, Pageable pageable);
}

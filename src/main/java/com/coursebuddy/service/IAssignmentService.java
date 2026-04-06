package com.coursebuddy.service;

import com.coursebuddy.domain.dto.AssignmentDTO;
import com.coursebuddy.domain.vo.AssignmentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 作业服务
 */
public interface IAssignmentService {
    AssignmentVO createAssignment(Long courseId, AssignmentDTO dto);
    AssignmentVO updateAssignment(Long id, AssignmentDTO dto);
    void deleteAssignment(Long id);
    AssignmentVO getAssignment(Long id);
    Page<AssignmentVO> listAssignments(Long courseId, Pageable pageable);
    AssignmentVO publishAssignment(Long id);
    List<Object> getAllAssignmentsIncludingDeleted(Long courseId);
}

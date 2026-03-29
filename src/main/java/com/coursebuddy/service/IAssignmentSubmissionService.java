package com.coursebuddy.service;

import com.coursebuddy.domain.dto.AssignmentSubmissionDTO;
import com.coursebuddy.domain.dto.GradeSubmissionDTO;
import com.coursebuddy.domain.vo.AssignmentSubmissionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAssignmentSubmissionService {
    AssignmentSubmissionVO submitAssignment(Long assignmentId, AssignmentSubmissionDTO dto);
    AssignmentSubmissionVO updateSubmission(Long id, AssignmentSubmissionDTO dto);
    AssignmentSubmissionVO getSubmission(Long id);
    Page<AssignmentSubmissionVO> listSubmissions(Long assignmentId, Pageable pageable);
    AssignmentSubmissionVO gradeSubmission(Long id, GradeSubmissionDTO dto);
    AssignmentSubmissionVO getMySubmission(Long assignmentId);
}

package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CourseDiscussionDTO;
import com.coursebuddy.domain.vo.CourseDiscussionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseDiscussionService {
    CourseDiscussionVO create(CourseDiscussionDTO dto);
    Page<CourseDiscussionVO> listByCourse(Long courseId, Pageable pageable);
    CourseDiscussionVO getById(Long id);
    CourseDiscussionVO update(Long id, CourseDiscussionDTO dto);
    void delete(Long id);
    CourseDiscussionVO like(Long id);
    CourseDiscussionVO pin(Long id);
}

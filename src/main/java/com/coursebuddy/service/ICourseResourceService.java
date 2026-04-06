package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CourseResourceDTO;
import com.coursebuddy.domain.vo.CourseResourceVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 课程资源服务
 */
public interface ICourseResourceService {
    CourseResourceVO createResource(Long courseId, CourseResourceDTO dto);
    void deleteResource(Long resourceId);
    CourseResourceVO getResource(Long resourceId);
    Page<CourseResourceVO> listResources(Long courseId, Pageable pageable);
}

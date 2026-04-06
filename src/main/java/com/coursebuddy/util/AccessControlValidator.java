package com.coursebuddy.util;

import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.domain.po.CoursePO;
import com.coursebuddy.mapper.CourseCatalogMapper;
import com.coursebuddy.mapper.CourseEnrollmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 统一的权限验证工具类
 * 提供课程、团队等资源的权限检查方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessControlValidator {

    private final CourseCatalogMapper courseRepository;
    private final CourseEnrollmentMapper enrollmentRepository;

    /**
     * 验证用户是否为指定课程的讲师
     *
     * @param courseId 课程ID
     * @param userId 用户ID
    * @throws BusinessException 如果用户不是讲师或课程不存在
     */
    public void validateCourseInstructor(Long courseId, Long userId) {
        CoursePO course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }
        if (!course.getInstructorId().equals(userId)) {
            log.warn("Access denied: User {} is not instructor of course {}", userId, courseId);
            throw new BusinessException(403, "Not authorized: User is not course instructor");
        }
    }

    /**
     * 验证用户是否拥有教学权限（讲师或全局TA）
     * 用于作业、资源等教学内容的操作权限
     *
     * @param courseId 课程ID
     * @param user 当前用户（包含role信息）
     * @throws BusinessException 如果用户没有教学权限
     */
    public void validateCourseTeacherAuthority(Long courseId, User user) {
        CoursePO course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }
        
        // 检查是否为讲师或全局TA
        boolean isInstructor = course.getInstructorId().equals(user.getId());
        boolean isTA = Role.TA.equals(user.getRole());
        
        if (!isInstructor && !isTA) {
            log.warn("Access denied: User {} (role={}) lacks teaching authority for course {}", 
                    user.getId(), user.getRole(), courseId);
            throw new BusinessException(403, "Not authorized: Only instructors and TAs can perform this action");
        }
    }

    /**
     * 验证用户是否为指定课程的成员（讲师或学生）
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @throws BusinessException 如果用户不是课程成员
     */
    public void validateCourseMember(Long courseId, Long userId) {
        CoursePO course = courseRepository.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }

        boolean isInstructor = course.getInstructorId().equals(userId);
        boolean isStudent = enrollmentRepository.existsByCourseIdAndUserId(courseId, userId);

        if (!isInstructor && !isStudent) {
            log.warn("Access denied: User {} is not member of course {}", userId, courseId);
            throw new BusinessException(403, "Not authorized: User is not course member");
        }
    }

    /**
     * 验证用户是否可以查看指定课程（讲师、TA或学生）
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return 是否有权限查看
     */
    public boolean canViewCourse(Long courseId, Long userId) {
        CoursePO course = courseRepository.selectById(courseId);
        if (course == null) {
            return false;
        }

        boolean isInstructor = course.getInstructorId().equals(userId);
        boolean isStudent = enrollmentRepository.existsByCourseIdAndUserId(courseId, userId);

        return isInstructor || isStudent;
    }

    /**
     * 验证资源是否属于指定的课程
     *
     * @param resourceCourseId 资源关联的课程ID
     * @param expectedCourseId 预期的课程ID
     * @throws BusinessException 如果资源不属于指定课程
     */
    public void validateResourceCourseOwnership(Long resourceCourseId, Long expectedCourseId) {
        if (!resourceCourseId.equals(expectedCourseId)) {
            throw new BusinessException(403, "Resource does not belong to this course");
        }
    }

    /**
     * 验证两个ID是否相等（用于所有权检查）
     *
     * @param actualId 实际ID
     * @param expectedId 预期ID
     * @param resourceName 资源名称（用于日志）
     * @throws BusinessException 如果ID不相等
     */
    public void validateOwnership(Long actualId, Long expectedId, String resourceName) {
        if (!actualId.equals(expectedId)) {
            throw new BusinessException(403, "Not authorized to access this " + resourceName);
        }
    }
}

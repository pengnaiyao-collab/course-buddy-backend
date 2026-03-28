package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CoursePermissionDTO;
import com.coursebuddy.domain.vo.CoursePermissionVO;

import java.util.List;

/**
 * 课程权限管理服务接口
 * 四级权限体系:
 * L1 - 课程库管理员（增删改查、授权管理）
 * L2 - 核心协作成员（内容编辑、审核参与）
 * L3 - 选课班级成员（查看内容、提问互动）
 * L4 - 校内访客成员（只读访问）
 */
public interface ICoursePermissionService {

    /** 授予用户课程权限 */
    CoursePermissionVO grantPermission(CoursePermissionDTO dto);

    /** 更新用户课程权限级别 */
    CoursePermissionVO updatePermission(CoursePermissionDTO dto);

    /** 撤销用户课程权限 */
    void revokePermission(Long userId, Long courseId);

    /** 获取课程内所有成员的权限 */
    List<CoursePermissionVO> getCourseMembers(Long courseId);

    /** 获取用户的所有课程权限 */
    List<CoursePermissionVO> getUserPermissions(Long userId);

    /** 获取课程内特定级别的成员 */
    List<CoursePermissionVO> getCourseAdmins(Long courseId);

    /** 检查当前用户对指定课程是否具有至少指定级别的权限 */
    boolean hasPermission(Long courseId, String minLevel);

    /** 获取当前用户在指定课程中的权限级别，无权限返回 null */
    String getPermissionLevel(Long courseId);
}

package com.coursebuddy.service;

import com.coursebuddy.domain.dto.CourseActionPermissionUpdateDTO;
import com.coursebuddy.domain.dto.CoursePermissionDTO;
import com.coursebuddy.domain.vo.CourseActionPermissionVO;
import com.coursebuddy.domain.vo.CourseAdminVoteVO;
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

    /** 获取课程细粒度动作权限矩阵 */
    List<CourseActionPermissionVO> getActionPermissionMatrix(Long courseId);

    /** 更新某一级别在某动作上的权限开关 */
    CourseActionPermissionVO updateActionPermission(CourseActionPermissionUpdateDTO dto);

    /** 检查当前用户是否拥有某动作权限 */
    boolean hasActionPermission(Long courseId, String actionKey);

    /** 对候选管理员投票（学生自治） */
    CourseAdminVoteVO voteAdmin(Long courseId, Long candidateUserId);

    /** 管理员轮值：将候选人提升为 L1，现有 L1 退为 L2 */
    CourseAdminVoteVO rotateAdmin(Long courseId, Long newAdminUserId);
}

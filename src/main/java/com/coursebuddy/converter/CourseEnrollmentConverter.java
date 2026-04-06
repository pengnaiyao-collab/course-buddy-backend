package com.coursebuddy.converter;

import com.coursebuddy.domain.po.CourseEnrollmentPO;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.domain.vo.CourseEnrollmentVO;
import com.coursebuddy.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程选课转换器
 */
@Component
public class CourseEnrollmentConverter {

    private final UserMapper userMapper;

    public CourseEnrollmentConverter(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CourseEnrollmentVO poToVo(CourseEnrollmentPO po) {
        if (po == null) return null;
        return CourseEnrollmentVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .userId(po.getUserId())
                .studentId(po.getUserId())
                .status(po.getStatus())
                .enrolledAt(po.getEnrolledAt())
                .droppedAt(po.getDroppedAt())
                .completedAt(po.getCompletedAt())
                .build();
    }

    public CourseEnrollmentVO poToVoWithUser(CourseEnrollmentPO po, UserPO user) {
        if (po == null) return null;
        return CourseEnrollmentVO.builder()
                .id(po.getId())
                .courseId(po.getCourseId())
                .userId(po.getUserId())
                .studentId(po.getUserId())
                .studentName(user != null ? user.getRealName() : null)
                .username(user != null ? user.getUsername() : null)
                .status(po.getStatus())
                .enrolledAt(po.getEnrolledAt())
                .droppedAt(po.getDroppedAt())
                .completedAt(po.getCompletedAt())
                .build();
    }

    public List<CourseEnrollmentVO> poListToVoList(List<CourseEnrollmentPO> list) {
        if (list == null) return null;
        return list.stream().map(this::poToVo).collect(Collectors.toList());
    }

    public List<CourseEnrollmentVO> poListToVoListWithUsers(List<CourseEnrollmentPO> list) {
        if (list == null || list.isEmpty()) return null;
        
        // 获取所有用户 ID
        List<Long> userIds = list.stream()
                .map(CourseEnrollmentPO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户
        Map<Long, UserPO> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserPO::getId, user -> user));
        
        // 转换为视图对象，并关联用户信息
        return list.stream()
                .map(po -> poToVoWithUser(po, userMap.get(po.getUserId())))
                .collect(Collectors.toList());
    }

    public Page<CourseEnrollmentVO> poPageToVoPage(Page<CourseEnrollmentPO> page) {
        return page.map(this::poToVo);
    }

    public Page<CourseEnrollmentVO> poPageToVoPageWithUsers(Page<CourseEnrollmentPO> page) {
        if (page == null || page.getContent().isEmpty()) return page.map(this::poToVo);
        
        List<CourseEnrollmentPO> content = page.getContent();
        List<Long> userIds = content.stream()
                .map(CourseEnrollmentPO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, UserPO> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserPO::getId, user -> user));
        
        return page.map(po -> poToVoWithUser(po, userMap.get(po.getUserId())));
    }
}

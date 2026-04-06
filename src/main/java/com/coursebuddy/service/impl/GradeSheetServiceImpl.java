package com.coursebuddy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.GradeUpdateDTO;
import com.coursebuddy.domain.po.GradeSheetPO;
import com.coursebuddy.domain.vo.GradeSheetVO;
import com.coursebuddy.converter.GradeSheetConverter;
import com.coursebuddy.mapper.GradeSheetMapper;
import com.coursebuddy.mapper.UserMapper;
import com.coursebuddy.domain.po.UserPO;
import com.coursebuddy.service.IGradeSheetService;
import com.coursebuddy.util.AccessControlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成绩成绩单服务实现
 */
@Service
@RequiredArgsConstructor
public class GradeSheetServiceImpl implements IGradeSheetService {

    private final GradeSheetMapper gradeSheetRepository;
    private final GradeSheetConverter gradeSheetMapper;
    private final AccessControlValidator accessControlValidator;
    private final UserMapper userRepository;

    @Override
    @Transactional
    public GradeSheetVO getOrCreateGradeSheet(Long courseId, Long studentId) {
        GradeSheetPO po = gradeSheetRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseGet(() -> {
                    GradeSheetPO newPo = GradeSheetPO.builder()
                            .courseId(courseId)
                            .studentId(studentId)
                            .build();
                    gradeSheetRepository.insert(newPo);
                    return newPo;
                });
        return gradeSheetMapper.poToVo(po);
    }

    @Override
    @Transactional
    public GradeSheetVO updateGradeSheet(Long courseId, Long studentId, GradeUpdateDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!Role.TEACHER.equals(currentUser.getRole()) && !Role.TA.equals(currentUser.getRole())) {
            throw new BusinessException(403, "Only teachers and TAs can update grade sheets");
        }
        // 验证当前用户是否为该课程的讲师或TA
        accessControlValidator.validateCourseInstructor(courseId, currentUser.getId());
        GradeSheetPO po = gradeSheetRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseGet(() -> {
                    GradeSheetPO created = GradeSheetPO.builder()
                            .courseId(courseId)
                            .studentId(studentId)
                            .build();
                    gradeSheetRepository.insert(created);
                    return created;
                });
        if (dto.getAssignmentScore() != null) po.setAssignmentScore(dto.getAssignmentScore());
        if (dto.getParticipationScore() != null) po.setParticipationScore(dto.getParticipationScore());
        if (dto.getQuizScore() != null) po.setQuizScore(dto.getQuizScore());
        if (dto.getMidtermScore() != null) po.setMidtermScore(dto.getMidtermScore());
        if (dto.getFinalScore() != null) po.setFinalScore(dto.getFinalScore());
        if (dto.getComments() != null) po.setComments(dto.getComments());

        int total = calculateTotalScore(po);
        po.setTotalScore(total);
        po.setGrade(calculateLetterGrade(total));
        po.setGradeDate(LocalDateTime.now());

        gradeSheetRepository.updateById(po);
        return gradeSheetMapper.poToVo(po);
    }

    @Override
    @Transactional
    public GradeSheetVO getMyGrade(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (!Role.STUDENT.equals(currentUser.getRole())) {
            throw new BusinessException(403, "Only students can view their grade sheet");
        }
        // 确保用户是课程成员，避免越权访问
        accessControlValidator.validateCourseMember(courseId, currentUser.getId());
        GradeSheetPO po = gradeSheetRepository.findByCourseIdAndStudentId(courseId, currentUser.getId())
                .orElseGet(() -> {
                    GradeSheetPO created = GradeSheetPO.builder()
                            .courseId(courseId)
                            .studentId(currentUser.getId())
                            .build();
                    gradeSheetRepository.insert(created);
                    return created;
                });
        GradeSheetVO vo = gradeSheetMapper.poToVo(po);
        vo.setStudentName(resolveStudentName(currentUser.getId()));
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeSheetVO> listCourseGrades(Long courseId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        // 仅允许课程讲师或TA查看成绩列表
        accessControlValidator.validateCourseTeacherAuthority(courseId, currentUser);
        IPage<GradeSheetPO> poPage = gradeSheetRepository.findByCourseId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        Page<GradeSheetVO> voPage = gradeSheetMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        attachStudentNames(voPage.getContent());
        return voPage;
    }

    private void attachStudentNames(List<GradeSheetVO> grades) {
        if (grades == null || grades.isEmpty()) {
            return;
        }
        List<Long> studentIds = grades.stream()
                .map(GradeSheetVO::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserPO> usersById = userRepository.selectBatchIds(studentIds)
                .stream()
                .collect(Collectors.toMap(UserPO::getId, user -> user));
        for (GradeSheetVO grade : grades) {
            UserPO user = usersById.get(grade.getStudentId());
            grade.setStudentName(resolveStudentName(user));
        }
    }

    private String resolveStudentName(Long studentId) {
        UserPO user = userRepository.selectById(studentId);
        return resolveStudentName(user);
    }

    private String resolveStudentName(UserPO user) {
        if (user == null) {
            return "学生";
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

    private int calculateTotalScore(GradeSheetPO po) {
        int total = 0;
        if (po.getAssignmentScore() != null) total += po.getAssignmentScore();
        if (po.getParticipationScore() != null) total += po.getParticipationScore();
        if (po.getQuizScore() != null) total += po.getQuizScore();
        if (po.getMidtermScore() != null) total += po.getMidtermScore();
        if (po.getFinalScore() != null) total += po.getFinalScore();
        return total;
    }

    private static final int GRADE_A_THRESHOLD = 90;
    private static final int GRADE_B_THRESHOLD = 80;
    private static final int GRADE_C_THRESHOLD = 70;
    private static final int GRADE_D_THRESHOLD = 60;

    private String calculateLetterGrade(int totalScore) {
        if (totalScore >= GRADE_A_THRESHOLD) return "A";
        if (totalScore >= GRADE_B_THRESHOLD) return "B";
        if (totalScore >= GRADE_C_THRESHOLD) return "C";
        if (totalScore >= GRADE_D_THRESHOLD) return "D";
        return "F";
    }
}

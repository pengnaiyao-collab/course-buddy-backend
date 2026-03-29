package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.GradeUpdateDTO;
import com.coursebuddy.domain.po.GradeSheetPO;
import com.coursebuddy.domain.vo.GradeSheetVO;
import com.coursebuddy.mapper.GradeSheetMapper;
import com.coursebuddy.repository.GradeSheetRepository;
import com.coursebuddy.service.IGradeSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GradeSheetServiceImpl implements IGradeSheetService {

    private final GradeSheetRepository gradeSheetRepository;
    private final GradeSheetMapper gradeSheetMapper;

    @Override
    @Transactional
    public GradeSheetVO getOrCreateGradeSheet(Long courseId, Long studentId) {
        GradeSheetPO po = gradeSheetRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseGet(() -> {
                    GradeSheetPO newPo = GradeSheetPO.builder()
                            .courseId(courseId)
                            .studentId(studentId)
                            .build();
                    return gradeSheetRepository.save(newPo);
                });
        return gradeSheetMapper.poToVo(po);
    }

    @Override
    @Transactional
    public GradeSheetVO updateGradeSheet(Long courseId, Long studentId, GradeUpdateDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can update grade sheets");
        }
        GradeSheetPO po = gradeSheetRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseGet(() -> gradeSheetRepository.save(GradeSheetPO.builder()
                        .courseId(courseId)
                        .studentId(studentId)
                        .build()));
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

        return gradeSheetMapper.poToVo(gradeSheetRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public GradeSheetVO getMyGrade(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        GradeSheetPO po = gradeSheetRepository.findByCourseIdAndStudentId(courseId, currentUser.getId())
                .orElseThrow(() -> new BusinessException(404, "Grade sheet not found"));
        return gradeSheetMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GradeSheetVO> listCourseGrades(Long courseId, Pageable pageable) {
        return gradeSheetMapper.poPageToVoPage(gradeSheetRepository.findByCourseId(courseId, pageable));
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

    private String calculateLetterGrade(int totalScore) {
        if (totalScore >= 90) return "A";
        if (totalScore >= 80) return "B";
        if (totalScore >= 70) return "C";
        if (totalScore >= 60) return "D";
        return "F";
    }
}

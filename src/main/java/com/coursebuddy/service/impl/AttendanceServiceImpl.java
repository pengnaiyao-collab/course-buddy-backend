package com.coursebuddy.service.impl;

import com.coursebuddy.auth.Role;
import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.AttendanceMarkDTO;
import com.coursebuddy.domain.po.AttendancePO;
import com.coursebuddy.domain.vo.AttendanceStatsVO;
import com.coursebuddy.domain.vo.AttendanceVO;
import com.coursebuddy.mapper.AttendanceMapper;
import com.coursebuddy.repository.AttendanceRepository;
import com.coursebuddy.service.IAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public void markAttendance(Long courseId, LocalDate sessionDate, List<AttendanceMarkDTO.AttendanceEntryDTO> entries) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(403, "Only teachers and admins can mark attendance");
        }
        for (AttendanceMarkDTO.AttendanceEntryDTO entry : entries) {
            AttendancePO po = attendanceRepository
                    .findByCourseIdAndStudentIdAndSessionDate(courseId, entry.getStudentId(), sessionDate)
                    .orElseGet(() -> AttendancePO.builder()
                            .courseId(courseId)
                            .studentId(entry.getStudentId())
                            .sessionDate(sessionDate)
                            .build());
            po.setStatus(entry.getStatus() != null ? entry.getStatus() : "PRESENT");
            po.setRemarks(entry.getRemarks());
            attendanceRepository.save(po);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceVO> getMyAttendance(Long courseId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return attendanceMapper.poPageToVoPage(
                attendanceRepository.findByCourseIdAndStudentId(courseId, currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceVO> getCourseAttendance(Long courseId, Pageable pageable) {
        return attendanceMapper.poPageToVoPage(attendanceRepository.findByCourseId(courseId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatsVO getMyAttendanceStats(Long courseId) {
        User currentUser = SecurityUtils.getCurrentUser();
        Long studentId = currentUser.getId();
        long total = attendanceRepository.countByCourseIdAndStudentId(courseId, studentId);
        long present = attendanceRepository.countByCourseIdAndStudentIdAndStatus(courseId, studentId, "PRESENT");
        long absent = attendanceRepository.countByCourseIdAndStudentIdAndStatus(courseId, studentId, "ABSENT");
        long late = attendanceRepository.countByCourseIdAndStudentIdAndStatus(courseId, studentId, "LATE");
        long excused = attendanceRepository.countByCourseIdAndStudentIdAndStatus(courseId, studentId, "EXCUSED");
        double rate = total > 0 ? (double) present / total * 100 : 0.0;
        return AttendanceStatsVO.builder()
                .courseId(courseId)
                .studentId(studentId)
                .totalSessions(total)
                .presentCount(present)
                .absentCount(absent)
                .lateCount(late)
                .excusedCount(excused)
                .attendanceRate(rate)
                .build();
    }
}

package com.coursebuddy.service.impl;

import com.coursebuddy.domain.auth.Role;
import com.coursebuddy.domain.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.AttendanceMarkDTO;
import com.coursebuddy.domain.po.AttendancePO;
import com.coursebuddy.domain.vo.AttendanceStatsVO;
import com.coursebuddy.domain.vo.AttendanceVO;
import com.coursebuddy.domain.vo.CheckInCodeVO;
import com.coursebuddy.converter.AttendanceConverter;
import com.coursebuddy.mapper.AttendanceMapper;
import com.coursebuddy.service.IAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 考勤服务实现
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    private final AttendanceMapper attendanceRepository;
    private final AttendanceConverter attendanceMapper;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final String ATTENDANCE_CODE_KEY_PREFIX = "attendance:code:";

    @Override
    @Transactional
    public void markAttendance(Long courseId, LocalDate sessionDate, List<AttendanceMarkDTO.AttendanceEntryDTO> entries) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER && currentUser.getRole() != Role.TA) {
            throw new BusinessException(403, "Only teachers and TAs can mark attendance");
        }
        for (AttendanceMarkDTO.AttendanceEntryDTO entry : entries) {
            Long studentId = entry.getStudentId();
            attendanceRepository.delete(new LambdaQueryWrapper<AttendancePO>()
                    .eq(AttendancePO::getCourseId, courseId)
                    .eq(AttendancePO::getStudentId, studentId)
                    .eq(AttendancePO::getSessionDate, sessionDate));

            AttendancePO po = AttendancePO.builder()
                    .courseId(courseId)
                    .studentId(studentId)
                    .sessionDate(sessionDate)
                    .status(entry.getStatus() != null ? entry.getStatus() : "PRESENT")
                    .remarks(entry.getRemarks())
                    .build();
            attendanceRepository.insert(po);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceVO> getMyAttendance(Long courseId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<AttendancePO> poPage = attendanceRepository.findByCourseIdAndStudentId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId, currentUser.getId());
        return attendanceMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceVO> getMyAttendanceByDate(Long courseId, LocalDate sessionDate) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<AttendancePO> records = attendanceRepository.findByCourseIdAndStudentIdAndSessionDateList(
                courseId, currentUser.getId(), sessionDate);
        return attendanceMapper.poListToVoList(records);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceVO> getCourseAttendance(Long courseId, Pageable pageable) {
        IPage<AttendancePO> poPage = attendanceRepository.findByCourseId(
                MybatisPlusPageUtils.toMpPage(pageable), courseId);
        return attendanceMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceVO> getCourseAttendanceByDate(Long courseId, LocalDate sessionDate) {
        List<AttendancePO> records = attendanceRepository.findByCourseIdAndSessionDate(courseId, sessionDate);
        return attendanceMapper.poListToVoList(records);
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

    @Override
    public CheckInCodeVO generateCheckInCode(Long courseId) {
        String code = String.format("%06d", secureRandom.nextInt(1000000));
        redisTemplate.opsForValue().set(ATTENDANCE_CODE_KEY_PREFIX + courseId, code, 5, TimeUnit.MINUTES);
        return CheckInCodeVO.builder()
                .code(code)
                .remainingSeconds(300L)
                .build();
    }

    @Override
    public CheckInCodeVO getCheckInCode(Long courseId) {
        String code = redisTemplate.opsForValue().get(ATTENDANCE_CODE_KEY_PREFIX + courseId);
        if (code == null) {
            return null;
        }
        Long ttl = redisTemplate.getExpire(ATTENDANCE_CODE_KEY_PREFIX + courseId, TimeUnit.SECONDS);
        return CheckInCodeVO.builder()
                .code(code)
                .remainingSeconds(ttl > 0 ? ttl : 0)
                .build();
    }

    @Override
    @Transactional
    public void checkInWithCode(Long courseId, String code) {
        User currentUser = SecurityUtils.getCurrentUser();
        String savedCode = redisTemplate.opsForValue().get(ATTENDANCE_CODE_KEY_PREFIX + courseId);
        
        if (savedCode == null) {
            throw new BusinessException(400, "Check-in code has expired");
        }
        if (!savedCode.equals(code)) {
            throw new BusinessException(400, "Invalid check-in code");
        }

        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByCourseIdAndStudentIdAndSessionDate(courseId, currentUser.getId(), today).isPresent()) {
            throw new BusinessException(409, "You have already checked in today");
        }

        AttendancePO po = AttendancePO.builder()
                .courseId(courseId)
                .studentId(currentUser.getId())
                .sessionDate(today)
                .status("PRESENT")
            .remarks("签到码签到")
                .build();
        attendanceRepository.insert(po);
    }
}

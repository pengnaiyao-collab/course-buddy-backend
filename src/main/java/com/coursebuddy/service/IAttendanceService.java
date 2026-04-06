package com.coursebuddy.service;

import com.coursebuddy.domain.dto.AttendanceMarkDTO;
import com.coursebuddy.domain.vo.AttendanceStatsVO;
import com.coursebuddy.domain.vo.AttendanceVO;
import com.coursebuddy.domain.vo.CheckInCodeVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤服务
 */
public interface IAttendanceService {
    void markAttendance(Long courseId, LocalDate sessionDate, List<AttendanceMarkDTO.AttendanceEntryDTO> entries);
    Page<AttendanceVO> getMyAttendance(Long courseId, Pageable pageable);
    List<AttendanceVO> getMyAttendanceByDate(Long courseId, LocalDate sessionDate);
    Page<AttendanceVO> getCourseAttendance(Long courseId, Pageable pageable);
    List<AttendanceVO> getCourseAttendanceByDate(Long courseId, LocalDate sessionDate);
    AttendanceStatsVO getMyAttendanceStats(Long courseId);
    CheckInCodeVO generateCheckInCode(Long courseId);
    CheckInCodeVO getCheckInCode(Long courseId);
    void checkInWithCode(Long courseId, String code);
}

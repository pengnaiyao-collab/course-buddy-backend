package com.coursebuddy.service;

import com.coursebuddy.domain.dto.AttendanceMarkDTO;
import com.coursebuddy.domain.vo.AttendanceStatsVO;
import com.coursebuddy.domain.vo.AttendanceVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IAttendanceService {
    void markAttendance(Long courseId, LocalDate sessionDate, List<AttendanceMarkDTO.AttendanceEntryDTO> entries);
    Page<AttendanceVO> getMyAttendance(Long courseId, Pageable pageable);
    Page<AttendanceVO> getCourseAttendance(Long courseId, Pageable pageable);
    AttendanceStatsVO getMyAttendanceStats(Long courseId);
}

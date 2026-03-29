package com.coursebuddy.domain.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceMarkDTO {
    private LocalDate sessionDate;
    private List<AttendanceEntryDTO> entries;

    @Data
    public static class AttendanceEntryDTO {
        private Long studentId;
        private String status;
        private String remarks;
    }
}

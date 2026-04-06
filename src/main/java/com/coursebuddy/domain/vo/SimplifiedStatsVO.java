package com.coursebuddy.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 统计视图对象
 */
@Data
@Builder
public class SimplifiedStatsVO {
    private Double average;
    private Double passRate;
    private List<DistributionEntry> distribution;

    @Data
    @Builder
    public static class DistributionEntry {
        private String range;
        private Long count;
    }
}

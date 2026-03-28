package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDiffVO {
    private String entityType;
    private Long entityId;
    private int versionA;
    private int versionB;
    /** Unified diff format as a list of lines */
    private List<String> diffLines;
    /** Number of added lines */
    private long addedLines;
    /** Number of removed lines */
    private long removedLines;
}

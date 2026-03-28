package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 笔记版本差异比较视图对象。
 *
 * <p>包含两个版本的基本信息及逐行差异内容。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteVersionDiffVO {

    /** 版本 A 的基本信息。 */
    private NoteVersionVO versionA;

    /** 版本 B 的基本信息。 */
    private NoteVersionVO versionB;

    /**
     * 统一差异格式（Unified Diff）字符串。
     *
     * <p>格式参考 GNU diff -u 输出：
     * {@code ---}, {@code +++}, {@code @@}, 上下文行与增删行。</p>
     */
    private String unifiedDiff;

    /**
     * 逐行差异列表，每个元素描述一行的变化类型和内容。
     */
    private List<DiffLine> lines;

    /**
     * 单行差异描述。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiffLine {

        /**
         * 变化类型：{@code EQUAL}（相同）、{@code INSERT}（新增）、{@code DELETE}（删除）。
         */
        private String type;

        /** 行内容。 */
        private String content;

        /** 版本 A 中的行号（DELETE/EQUAL 有效，INSERT 为 null）。 */
        private Integer lineA;

        /** 版本 B 中的行号（INSERT/EQUAL 有效，DELETE 为 null）。 */
        private Integer lineB;
    }
}

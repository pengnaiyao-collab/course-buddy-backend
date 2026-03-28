package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphVO {
    private List<GraphNodeVO> nodes;
    private List<GraphEdgeVO> edges;
    private Integer totalNodes;
    private Integer totalEdges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphNodeVO {
        private Long id;
        private String label;
        private String category;
        private String description;
        private Map<String, Object> properties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphEdgeVO {
        private Long id;
        private Long source;
        private Long target;
        private String relationType;
        private String description;
    }
}

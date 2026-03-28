package com.coursebuddy.util;

import org.springframework.stereotype.Component;

/**
 * 提示词工程工具 - 优化发送给 AI 的提示词
 */
@Component
public class PromptEngineering {

    private static final String SYSTEM_INSTRUCTION =
            "你是一位专业的教育助手，擅长帮助学生理解知识、复习课程内容和生成学习材料。" +
            "请用清晰、简洁、准确的语言回答问题，必要时使用结构化格式（如列表、标题）增强可读性。";

    /** 构建带系统指令的对话上下文 */
    public String addSystemContext(String userMessage) {
        return SYSTEM_INSTRUCTION + "\n\n用户问题：" + userMessage;
    }

    /** 优化复习提纲提示词 */
    public String buildOutlinePrompt(String subject, String requirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为以下主题生成一份详细的复习提纲，要求层次分明、条理清晰，包含主要知识点和子知识点。\n");
        sb.append("主题：").append(subject);
        if (requirements != null && !requirements.isBlank()) {
            sb.append("\n额外要求：").append(requirements);
        }
        sb.append("\n\n请按以下格式输出：\n一、[主要知识点]\n  1. [子知识点]\n  2. [子知识点]\n二、...");
        return sb.toString();
    }

    /** 优化考点汇总提示词 */
    public String buildExamPointsPrompt(String subject, String requirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("请总结以下主题的重要考点，按重要性从高到低排序，并简要说明每个考点的核心内容和常见题型。\n");
        sb.append("主题：").append(subject);
        if (requirements != null && !requirements.isBlank()) {
            sb.append("\n额外要求：").append(requirements);
        }
        return sb.toString();
    }

    /** 优化习题生成提示词 */
    public String buildQuestionsPrompt(String subject, int count, String difficulty, String requirements) {
        String diffLabel = switch (difficulty != null ? difficulty.toUpperCase() : "MEDIUM") {
            case "EASY" -> "简单";
            case "HARD" -> "难";
            default -> "中等";
        };
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("请为以下主题生成 %d 道%s难度的习题，包含题目和详细参考答案。", count, diffLabel));
        sb.append("题型可包括选择题、填空题和简答题，注意题目质量和实际考核价值。\n");
        sb.append("主题：").append(subject);
        if (requirements != null && !requirements.isBlank()) {
            sb.append("\n额外要求：").append(requirements);
        }
        return sb.toString();
    }

    /** 优化知识点拆解提示词 */
    public String buildBreakdownPrompt(String subject, String requirements) {
        StringBuilder sb = new StringBuilder();
        sb.append("请对以下知识点进行全面深入的拆解，包括：定义与概念、核心原理、关键要素、应用场景、常见误区和例题解析。\n");
        sb.append("知识点：").append(subject);
        if (requirements != null && !requirements.isBlank()) {
            sb.append("\n额外要求：").append(requirements);
        }
        return sb.toString();
    }

    /** 清理并截断过长的提示词 */
    public String sanitize(String prompt, int maxLength) {
        if (prompt == null) return "";
        String cleaned = prompt.trim().replaceAll("\\s{3,}", "\n\n");
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) + "..." : cleaned;
    }
}

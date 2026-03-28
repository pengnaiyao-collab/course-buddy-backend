package com.coursebuddy.service;

import com.coursebuddy.domain.dto.GenerateContentDTO;
import com.coursebuddy.domain.vo.GeneratedContentVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IContentGeneratorService {

    /** 生成复习提纲 */
    GeneratedContentVO generateOutline(GenerateContentDTO dto);

    /** 生成考点汇总 */
    GeneratedContentVO generateExamPoints(GenerateContentDTO dto);

    /** 生成习题 */
    GeneratedContentVO generateQuestions(GenerateContentDTO dto);

    /** 知识点拆解 */
    GeneratedContentVO generateBreakdown(GenerateContentDTO dto);

    /** 通用内容生成入口 */
    GeneratedContentVO generate(GenerateContentDTO dto);

    /** 获取当前用户的生成历史 */
    Page<GeneratedContentVO> listGeneratedContents(String contentType, Pageable pageable);

    /** 获取指定生成内容详情 */
    GeneratedContentVO getGeneratedContent(Long id);
}

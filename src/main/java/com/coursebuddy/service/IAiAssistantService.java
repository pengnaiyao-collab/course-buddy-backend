package com.coursebuddy.service;

import com.coursebuddy.domain.dto.LearningTaskDTO;
import com.coursebuddy.domain.dto.QuestionDTO;
import com.coursebuddy.domain.vo.AnswerVO;
import com.coursebuddy.domain.vo.LearningTaskVO;
import com.coursebuddy.domain.vo.QuestionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAiAssistantService {
    QuestionVO askQuestion(QuestionDTO dto);
    Page<QuestionVO> listMyQuestions(Pageable pageable);
    QuestionVO getQuestion(Long id);
    Page<AnswerVO> getAnswers(Long questionId, Pageable pageable);
    LearningTaskVO createTask(LearningTaskDTO dto);
    Page<LearningTaskVO> listMyTasks(String status, Pageable pageable);
    LearningTaskVO getTask(Long id);
    LearningTaskVO updateTask(Long id, LearningTaskDTO dto);
    void deleteTask(Long id);
}

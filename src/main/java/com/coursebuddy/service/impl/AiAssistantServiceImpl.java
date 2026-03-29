package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.LearningTaskDTO;
import com.coursebuddy.domain.dto.QuestionDTO;
import com.coursebuddy.domain.po.AnswerPO;
import com.coursebuddy.domain.po.LearningTaskPO;
import com.coursebuddy.domain.po.QuestionPO;
import com.coursebuddy.domain.vo.AnswerVO;
import com.coursebuddy.domain.vo.LearningTaskVO;
import com.coursebuddy.domain.vo.QuestionVO;
import com.coursebuddy.converter.AnswerConverter;
import com.coursebuddy.converter.LearningTaskConverter;
import com.coursebuddy.converter.QuestionConverter;
import com.coursebuddy.mapper.AnswerMapper;
import com.coursebuddy.mapper.LearningTaskMapper;
import com.coursebuddy.mapper.QuestionMapper;
import com.coursebuddy.service.IAiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements IAiAssistantService {

    private final QuestionMapper questionRepository;
    private final AnswerMapper answerRepository;
    private final LearningTaskMapper learningTaskRepository;
    private final QuestionConverter questionMapper;
    private final AnswerConverter answerMapper;
    private final LearningTaskConverter learningTaskMapper;

    @Override
    @Transactional
    public QuestionVO askQuestion(QuestionDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        QuestionPO po = questionMapper.dtoToPo(dto);
        po.setUserId(currentUser.getId());
        questionRepository.insert(po);

        AnswerPO answer = AnswerPO.builder()
                .questionId(po.getId())
                .content("AI is processing your question: " + po.getContent())
                .source("AI")
                .build();
        answerRepository.insert(answer);

        return questionMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionVO> listMyQuestions(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<QuestionPO> poPage = questionRepository.findByUserId(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return questionMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionVO getQuestion(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        QuestionPO po = questionRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Question not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this question");
        }
        return questionMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnswerVO> getAnswers(Long questionId, Pageable pageable) {
        if (questionRepository.selectById(questionId) == null) {
            throw new BusinessException(404, "Question not found");
        }
        IPage<AnswerPO> poPage = answerRepository.findByQuestionId(
                MybatisPlusPageUtils.toMpPage(pageable), questionId);
        return answerMapper.poPageToVoPage(MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional
    public LearningTaskVO createTask(LearningTaskDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        LearningTaskPO po = learningTaskMapper.dtoToPo(dto);
        po.setUserId(currentUser.getId());
        if (po.getStatus() == null) po.setStatus("PENDING");
        if (po.getPriority() == null) po.setPriority("MEDIUM");
        learningTaskRepository.insert(po);
        return learningTaskMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningTaskVO> listMyTasks(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (status != null && !status.isBlank()) {
            return learningTaskMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(
                            learningTaskRepository.findByUserIdAndStatus(
                                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), status),
                            pageable));
        }
        return learningTaskMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(
                        learningTaskRepository.findByUserId(
                                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId()),
                        pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public LearningTaskVO getTask(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        LearningTaskPO po = learningTaskRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Learning task not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this task");
        }
        return learningTaskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public LearningTaskVO updateTask(Long id, LearningTaskDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        LearningTaskPO po = learningTaskRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Learning task not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this task");
        }
        po.setTitle(dto.getTitle());
        po.setDescription(dto.getDescription());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        if (dto.getDueDate() != null) po.setDueDate(dto.getDueDate());
        if (dto.getPriority() != null) po.setPriority(dto.getPriority());
        learningTaskRepository.updateById(po);
        return learningTaskMapper.poToVo(po);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        LearningTaskPO po = learningTaskRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Learning task not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this task");
        }
        learningTaskRepository.deleteById(po.getId());
    }
}

package com.coursebuddy.aiassistant;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final LearningTaskRepository learningTaskRepository;

    @Transactional
    public QuestionResponse askQuestion(QuestionRequest request) {
        User currentUser = getCurrentUser();
        Question question = Question.builder()
                .userId(currentUser.getId())
                .courseId(request.getCourseId())
                .content(request.getContent())
                .subject(request.getSubject())
                .build();
        Question saved = questionRepository.save(question);

        // Generate a placeholder AI answer
        Answer answer = Answer.builder()
                .questionId(saved.getId())
                .content("AI is processing your question: " + request.getContent())
                .source("AI")
                .build();
        answerRepository.save(answer);

        return QuestionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<QuestionResponse> listMyQuestions(Pageable pageable) {
        User currentUser = getCurrentUser();
        return questionRepository.findByUserId(currentUser.getId(), pageable).map(QuestionResponse::from);
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long id) {
        User currentUser = getCurrentUser();
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Question not found"));
        if (!question.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this question");
        }
        return QuestionResponse.from(question);
    }

    @Transactional(readOnly = true)
    public Page<AnswerResponse> getAnswers(Long questionId, Pageable pageable) {
        if (!questionRepository.existsById(questionId)) {
            throw new BusinessException(404, "Question not found");
        }
        return answerRepository.findByQuestionId(questionId, pageable).map(AnswerResponse::from);
    }

    @Transactional
    public LearningTaskResponse createTask(LearningTaskRequest request) {
        User currentUser = getCurrentUser();
        LearningTask task = LearningTask.builder()
                .userId(currentUser.getId())
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .build();
        return LearningTaskResponse.from(learningTaskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<LearningTaskResponse> listMyTasks(String status, Pageable pageable) {
        User currentUser = getCurrentUser();
        if (status != null && !status.isBlank()) {
            return learningTaskRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable)
                    .map(LearningTaskResponse::from);
        }
        return learningTaskRepository.findByUserId(currentUser.getId(), pageable).map(LearningTaskResponse::from);
    }

    @Transactional(readOnly = true)
    public LearningTaskResponse getTask(Long id) {
        User currentUser = getCurrentUser();
        LearningTask task = learningTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Learning task not found"));
        if (!task.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this task");
        }
        return LearningTaskResponse.from(task);
    }

    @Transactional
    public LearningTaskResponse updateTask(Long id, LearningTaskRequest request) {
        User currentUser = getCurrentUser();
        LearningTask task = learningTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Learning task not found"));
        if (!task.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this task");
        }
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        return LearningTaskResponse.from(learningTaskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        User currentUser = getCurrentUser();
        LearningTask task = learningTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Learning task not found"));
        if (!task.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this task");
        }
        learningTaskRepository.delete(task);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new BusinessException(401, "User not authenticated");
    }
}

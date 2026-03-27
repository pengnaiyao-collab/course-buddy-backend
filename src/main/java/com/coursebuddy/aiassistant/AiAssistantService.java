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
    public Question askQuestion(Question question) {
        User currentUser = getCurrentUser();
        question.setUserId(currentUser.getId());
        Question saved = questionRepository.save(question);

        // Generate a placeholder AI answer
        Answer answer = Answer.builder()
                .questionId(saved.getId())
                .content("AI is processing your question: " + question.getContent())
                .source("AI")
                .build();
        answerRepository.save(answer);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Question> listMyQuestions(Pageable pageable) {
        User currentUser = getCurrentUser();
        return questionRepository.findByUserId(currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Question getQuestion(Long id) {
        User currentUser = getCurrentUser();
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Question not found"));
        if (!question.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this question");
        }
        return question;
    }

    @Transactional(readOnly = true)
    public Page<Answer> getAnswers(Long questionId, Pageable pageable) {
        if (!questionRepository.existsById(questionId)) {
            throw new BusinessException(404, "Question not found");
        }
        return answerRepository.findByQuestionId(questionId, pageable);
    }

    @Transactional
    public LearningTask createTask(LearningTask task) {
        User currentUser = getCurrentUser();
        task.setUserId(currentUser.getId());
        if (task.getStatus() == null) task.setStatus("PENDING");
        if (task.getPriority() == null) task.setPriority("MEDIUM");
        return learningTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Page<LearningTask> listMyTasks(String status, Pageable pageable) {
        User currentUser = getCurrentUser();
        if (status != null && !status.isBlank()) {
            return learningTaskRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
        }
        return learningTaskRepository.findByUserId(currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public LearningTask getTask(Long id) {
        User currentUser = getCurrentUser();
        LearningTask task = learningTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Learning task not found"));
        if (!task.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to view this task");
        }
        return task;
    }

    @Transactional
    public LearningTask updateTask(Long id, LearningTask item) {
        User currentUser = getCurrentUser();
        LearningTask task = learningTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Learning task not found"));
        if (!task.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this task");
        }
        task.setTitle(item.getTitle());
        task.setDescription(item.getDescription());
        if (item.getStatus() != null) task.setStatus(item.getStatus());
        if (item.getDueDate() != null) task.setDueDate(item.getDueDate());
        if (item.getPriority() != null) task.setPriority(item.getPriority());
        return learningTaskRepository.save(task);
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

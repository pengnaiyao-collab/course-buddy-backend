package com.coursebuddy.course;

import com.coursebuddy.auth.User;
import com.coursebuddy.auth.UserRepository;
import com.coursebuddy.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public Page<CourseResponse> listPublished(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return courseRepository.searchPublished(keyword, pageable).map(CourseResponse::from);
        }
        return courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable).map(CourseResponse::from);
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));
        return CourseResponse.from(course);
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        User teacher = getCurrentUser();
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .teacher(teacher)
                .price(request.getPrice())
                .category(request.getCategory())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT)
                .maxStudents(request.getMaxStudents())
                .coverImageUrl(request.getCoverImageUrl())
                .build();
        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));

        User currentUser = getCurrentUser();
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        if (request.getMaxStudents() != null) course.setMaxStudents(request.getMaxStudents());
        if (request.getCoverImageUrl() != null) course.setCoverImageUrl(request.getCoverImageUrl());

        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Course not found"));

        User currentUser = getCurrentUser();
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this course");
        }

        courseRepository.delete(course);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "User not found"));
    }
}

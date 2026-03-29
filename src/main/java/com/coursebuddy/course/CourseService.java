package com.coursebuddy.course;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coursebuddy.auth.User;
import com.coursebuddy.auth.AuthUserRepository;
import com.coursebuddy.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final AuthUserRepository userRepository;

    public IPage<CourseResponse> listPublished(String keyword, Page<Course> page) {
        IPage<Course> coursePage;
        if (keyword != null && !keyword.isBlank()) {
            coursePage = courseRepository.searchPublished(page, keyword);
        } else {
            coursePage = courseRepository.findByStatus(page, CourseStatus.PUBLISHED.name());
        }
        return coursePage.convert(this::toCourseResponse);
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }
        return toCourseResponse(course);
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        User teacher = getCurrentUser();
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .teacherId(teacher.getId())
                .price(request.getPrice())
                .category(request.getCategory())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.DRAFT)
                .maxStudents(request.getMaxStudents())
                .coverImageUrl(request.getCoverImageUrl())
                .build();
        courseRepository.insert(course);
        return toCourseResponse(course);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }

        User currentUser = getCurrentUser();
        if (course.getTeacherId() == null || !course.getTeacherId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        if (request.getMaxStudents() != null) course.setMaxStudents(request.getMaxStudents());
        if (request.getCoverImageUrl() != null) course.setCoverImageUrl(request.getCoverImageUrl());

        courseRepository.updateById(course);
        return toCourseResponse(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.selectById(id);
        if (course == null) {
            throw new BusinessException(404, "Course not found");
        }

        User currentUser = getCurrentUser();
        if (course.getTeacherId() == null || !course.getTeacherId().equals(currentUser.getId())) {
            throw new BusinessException(403, "You are not authorized to delete this course");
        }

        courseRepository.deleteById(course.getId());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "User not found"));
    }

    private CourseResponse toCourseResponse(Course course) {
        User teacher = null;
        if (course.getTeacherId() != null) {
            teacher = userRepository.selectById(course.getTeacherId());
        }
        return CourseResponse.from(course, teacher);
    }
}

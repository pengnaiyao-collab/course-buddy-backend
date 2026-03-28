package com.coursebuddy.service;

import com.coursebuddy.domain.dto.NotificationDTO;
import com.coursebuddy.domain.vo.NotificationVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {
    NotificationVO create(Long userId, NotificationDTO dto);
    Page<NotificationVO> listMyNotifications(Boolean isRead, String type, Pageable pageable);
    NotificationVO getById(Long id);
    NotificationVO markRead(Long id);
    int markAllRead();
    void delete(Long id);
    long countUnread();
}

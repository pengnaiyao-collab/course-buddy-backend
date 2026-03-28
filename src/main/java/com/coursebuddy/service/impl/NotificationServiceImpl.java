package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.NotificationDTO;
import com.coursebuddy.domain.po.NotificationPO;
import com.coursebuddy.domain.vo.NotificationVO;
import com.coursebuddy.mapper.NotificationMapper;
import com.coursebuddy.repository.NotificationRepository;
import com.coursebuddy.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationVO create(Long userId, NotificationDTO dto) {
        NotificationPO po = notificationMapper.dtoToPo(dto);
        po.setUserId(userId);
        return notificationMapper.poToVo(notificationRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationVO> listMyNotifications(Boolean isRead, String type, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (isRead != null && type != null) {
            return notificationMapper.poPageToVoPage(
                    notificationRepository.findByUserIdAndIsReadAndType(currentUser.getId(), isRead, type, pageable));
        }
        if (isRead != null) {
            return notificationMapper.poPageToVoPage(
                    notificationRepository.findByUserIdAndIsRead(currentUser.getId(), isRead, pageable));
        }
        if (type != null) {
            return notificationMapper.poPageToVoPage(
                    notificationRepository.findByUserIdAndType(currentUser.getId(), type, pageable));
        }
        return notificationMapper.poPageToVoPage(
                notificationRepository.findByUserId(currentUser.getId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationVO getById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotificationPO po = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Notification not found"));
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        return notificationMapper.poToVo(po);
    }

    @Override
    @Transactional
    public NotificationVO markRead(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotificationPO po = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Notification not found"));
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        po.setIsRead(true);
        return notificationMapper.poToVo(notificationRepository.save(po));
    }

    @Override
    @Transactional
    public int markAllRead() {
        User currentUser = SecurityUtils.getCurrentUser();
        return notificationRepository.markAllReadByUserId(currentUser.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotificationPO po = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Notification not found"));
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        notificationRepository.delete(po);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        User currentUser = SecurityUtils.getCurrentUser();
        return notificationRepository.countByUserIdAndIsRead(currentUser.getId(), false);
    }
}

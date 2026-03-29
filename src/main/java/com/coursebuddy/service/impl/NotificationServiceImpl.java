package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.NotificationDTO;
import com.coursebuddy.domain.po.NotificationPO;
import com.coursebuddy.domain.vo.NotificationVO;
import com.coursebuddy.converter.NotificationConverter;
import com.coursebuddy.mapper.NotificationMapper;
import com.coursebuddy.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMapper notificationRepository;
    private final NotificationConverter notificationMapper;

    @Override
    @Transactional
    public NotificationVO create(Long userId, NotificationDTO dto) {
        NotificationPO po = notificationMapper.dtoToPo(dto);
        po.setUserId(userId);
        notificationRepository.insert(po);
        return notificationMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationVO> listMyNotifications(Boolean isRead, String type, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (isRead != null && type != null) {
            IPage<NotificationPO> poPage = notificationRepository.findByUserIdAndIsReadAndType(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), isRead, type);
            return notificationMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        if (isRead != null) {
            IPage<NotificationPO> poPage = notificationRepository.findByUserIdAndIsRead(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), isRead);
            return notificationMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        if (type != null) {
            IPage<NotificationPO> poPage = notificationRepository.findByUserIdAndType(
                    MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), type);
            return notificationMapper.poPageToVoPage(
                    MybatisPlusPageUtils.toSpringPage(poPage, pageable));
        }
        IPage<NotificationPO> poPage = notificationRepository.findByUserId(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return notificationMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationVO getById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotificationPO po = notificationRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Notification not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        return notificationMapper.poToVo(po);
    }

    @Override
    @Transactional
    public NotificationVO markRead(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        NotificationPO po = notificationRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Notification not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        po.setIsRead(true);
        notificationRepository.updateById(po);
        return notificationMapper.poToVo(po);
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
        NotificationPO po = notificationRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Notification not found");
        }
        if (!po.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        notificationRepository.deleteById(po.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        User currentUser = SecurityUtils.getCurrentUser();
        return notificationRepository.countByUserIdAndIsRead(currentUser.getId(), false);
    }
}

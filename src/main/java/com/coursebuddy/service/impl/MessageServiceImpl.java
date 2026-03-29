package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.MybatisPlusPageUtils;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.coursebuddy.domain.dto.MessageDTO;
import com.coursebuddy.domain.po.MessagePO;
import com.coursebuddy.domain.vo.MessageVO;
import com.coursebuddy.converter.MessageConverter;
import com.coursebuddy.mapper.MessageMapper;
import com.coursebuddy.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageMapper messageRepository;
    private final MessageConverter messageMapper;

    @Override
    @Transactional
    public MessageVO sendMessage(MessageDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        MessagePO po = messageMapper.dtoToPo(dto);
        po.setSenderId(currentUser.getId());
        messageRepository.insert(po);
        return messageMapper.poToVo(po);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageVO> getConversation(Long otherUserId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<MessagePO> poPage = messageRepository.findConversation(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId(), otherUserId);
        return messageMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageVO> listInbox(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        IPage<MessagePO> poPage = messageRepository.findByReceiverIdOrderByCreatedAtDesc(
                MybatisPlusPageUtils.toMpPage(pageable), currentUser.getId());
        return messageMapper.poPageToVoPage(
                MybatisPlusPageUtils.toSpringPage(poPage, pageable));
    }

    @Override
    @Transactional
    public int markConversationRead(Long senderId) {
        User currentUser = SecurityUtils.getCurrentUser();
        return messageRepository.markConversationRead(senderId, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        User currentUser = SecurityUtils.getCurrentUser();
        return messageRepository.countByReceiverIdAndIsRead(currentUser.getId(), false);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        MessagePO po = messageRepository.selectById(id);
        if (po == null) {
            throw new BusinessException(404, "Message not found");
        }
        if (!po.getSenderId().equals(currentUser.getId()) && !po.getReceiverId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        messageRepository.deleteById(po.getId());
    }
}

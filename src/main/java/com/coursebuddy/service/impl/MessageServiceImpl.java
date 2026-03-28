package com.coursebuddy.service.impl;

import com.coursebuddy.auth.User;
import com.coursebuddy.common.SecurityUtils;
import com.coursebuddy.common.exception.BusinessException;
import com.coursebuddy.domain.dto.MessageDTO;
import com.coursebuddy.domain.po.MessagePO;
import com.coursebuddy.domain.vo.MessageVO;
import com.coursebuddy.mapper.MessageMapper;
import com.coursebuddy.repository.MessageRepository;
import com.coursebuddy.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageVO sendMessage(MessageDTO dto) {
        User currentUser = SecurityUtils.getCurrentUser();
        MessagePO po = messageMapper.dtoToPo(dto);
        po.setSenderId(currentUser.getId());
        return messageMapper.poToVo(messageRepository.save(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageVO> getConversation(Long otherUserId, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return messageMapper.poPageToVoPage(
                messageRepository.findConversation(currentUser.getId(), otherUserId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageVO> listInbox(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        return messageMapper.poPageToVoPage(
                messageRepository.findByReceiverIdOrderByCreatedAtDesc(currentUser.getId(), pageable));
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
        MessagePO po = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Message not found"));
        if (!po.getSenderId().equals(currentUser.getId()) && !po.getReceiverId().equals(currentUser.getId())) {
            throw new BusinessException(403, "Access denied");
        }
        messageRepository.delete(po);
    }
}

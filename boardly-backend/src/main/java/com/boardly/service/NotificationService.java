package com.boardly.service;

import com.boardly.common.dto.kanbanboard.BoardEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendBoardEvent(UUID boardId, BoardEvent event) {
        messagingTemplate.convertAndSend("/topic/board/" + boardId, event);
    }

    public void sendToUser(UUID userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
    }

    public void sendToTopic(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
}
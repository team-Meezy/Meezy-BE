package com.example.meezy.bc.collaboration.chat_message.application.service;

import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.chat_message.application.port.out.ChatMessagePublishPort;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.event.ChatMessageEvent;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.request.ChatMessageRequest;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.response.ChatMessageResponse;
import com.example.meezy.bc.collaboration.chat_message.domain.ChatMessage;
import com.example.meezy.bc.collaboration.chat_message.domain.repository.ChatMessageRepository;
import com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.out.redis.ChatMessageRateLimiter;
import com.example.meezy.bc.collaboration.chat_room.application.service.exception.ChatRoomNotFoundException;
import com.example.meezy.bc.collaboration.chat_room.domain.ChatRoom;
import com.example.meezy.bc.collaboration.chat_room.domain.repository.ChatRoomRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ChatMessagePublishPort chatMessagePublishPort;
    private final UserRepository userRepository;
    private final ChatMessageRateLimiter chatMessageRateLimiter;
    private final Clock appClock;

    @Transactional
    public ChatMessageResponse send(UUID teamId, UUID chatRoomId, ChatMessageRequest request){
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId_Value(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        validateTeamOwnership(chatRoom, teamId);

        AuthenticatedUser sender = currentUserQuery.currentUser();
        chatMessageRateLimiter.validate(sender.userId().value(), chatRoomId);
        User latestSender = userRepository.findByUserId(sender.userId())
                .orElseThrow(UserNotFoundException::new);
        LocalDateTime createdAt = LocalDateTime.now(appClock);

        ChatMessage chatMessage = ChatMessage.create(
                chatRoom.getChatRoomId(),
                latestSender.getName(),
                normalizeProfileImageUrl(latestSender.getProfileImageUrl()),
                request.content(),
                createdAt
        );
        chatMessageRepository.save(chatMessage);

        publishAfterCommit(chatMessage, chatRoomId);

        return ChatMessageResponse.from(chatMessage);
    }

    private void publishAfterCommit(ChatMessage chatMessage, UUID chatRoomId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                boolean success = chatMessagePublishPort.publish(ChatMessageEvent.from(chatMessage, chatRoomId));
                if (!success) {
                    log.warn("메시지 브로드캐스트 실패: chatMessageId={}, chatRoomId={}",
                            chatMessage.getChatMessageId().value(), chatRoomId);
                }
            }
        });
    }

    private String normalizeProfileImageUrl(String profileImageUrl) {
        return profileImageUrl == null ? "" : profileImageUrl;
    }

    private void validateTeamOwnership(ChatRoom chatRoom, UUID teamId){
        if (!chatRoom.getTeamId().equals(TeamId.of(teamId))) {
            throw new ChatRoomNotFoundException();
        }
    }
}

package com.example.meezy.bc.team.chat_message.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.chat_message.application.port.out.ChatMessagePublishPort;
import com.example.meezy.bc.team.chat_message.application.service.dto.event.ChatMessageEvent;
import com.example.meezy.bc.team.chat_message.application.service.dto.request.ChatMessageRequest;
import com.example.meezy.bc.team.chat_message.application.service.dto.response.ChatMessageResponse;
import com.example.meezy.bc.team.chat_message.domain.ChatMessage;
import com.example.meezy.bc.team.chat_message.domain.repository.ChatMessageRepository;
import com.example.meezy.bc.team.chat_room.application.service.exception.ChatRoomNotFoundException;
import com.example.meezy.bc.team.chat_room.domain.ChatRoom;
import com.example.meezy.bc.team.chat_room.domain.repository.ChatRoomRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ChatMessagePublishPort chatMessagePublishPort;

    @Transactional
    public ChatMessageResponse send(UUID teamId, UUID chatRoomId, ChatMessageRequest request){
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId_Value(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        validateTeamOwnership(chatRoom, teamId);

        String senderName = currentUserQuery.currentUser().name();

        ChatMessage chatMessage = ChatMessage.create(chatRoom.getChatRoomId(), senderName, request.content());
        chatMessageRepository.save(chatMessage);

        chatMessagePublishPort.publish(ChatMessageEvent.from(chatMessage, chatRoomId))
                .thenAccept(success -> {
                    if (!success) {
                        log.warn("메시지 브로드캐스트 실패: chatMessageId={}, chatRoomId={}",
                                chatMessage.getChatMessageId().value(), chatRoomId);
                    }
                });

        return ChatMessageResponse.from(chatMessage);
    }

    private void validateTeamOwnership(ChatRoom chatRoom, UUID teamId){
        if (!chatRoom.getTeamId().equals(TeamId.of(teamId))) {
            throw new ChatRoomNotFoundException();
        }
    }
}

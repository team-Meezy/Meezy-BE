package com.example.meezy.bc.collaboration.chat_message.domain.repository;

import com.example.meezy.bc.collaboration.chat_message.domain.ChatMessage;
import com.example.meezy.bc.collaboration.chat_message.domain.vo.ChatMessageId;
import com.example.meezy.bc.collaboration.chat_room.domain.vo.ChatRoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, ChatMessageId> {
    List<ChatMessage> findAllByChatRoomIdOrderByCreatedAtAscChatMessageIdAsc(ChatRoomId chatRoomId);
}

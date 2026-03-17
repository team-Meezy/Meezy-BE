package com.example.meezy.bc.collaboration.chat_message.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.collaboration.chat_message.domain.vo.ChatMessageId;
import com.example.meezy.bc.collaboration.chat_room.domain.vo.ChatRoomId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessage extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "chat_message_id"))
    private ChatMessageId chatMessageId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "chat_room_id"))
    private ChatRoomId chatRoomId;

    @Column(nullable = false)
    private String senderName;

    private String senderProfileImageUrl;

    @Column(nullable = false, columnDefinition = "VARCHAR(1000)")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static ChatMessage create(ChatRoomId chatRoomId, String senderName, String senderProfileImageUrl, String content){
        return ChatMessage.builder()
                .chatMessageId(ChatMessageId.newId())
                .chatRoomId(chatRoomId)
                .senderName(senderName)
                .senderProfileImageUrl(senderProfileImageUrl)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

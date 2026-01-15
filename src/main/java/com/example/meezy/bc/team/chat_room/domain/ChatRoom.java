package com.example.meezy.bc.team.chat_room.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.team.chat_room.domain.vo.ChatRoomId;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoom extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "chat_room_id"))
    private ChatRoomId chatRoomId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "team_id"))
    private TeamId teamId;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private String name;

    public static ChatRoom create(TeamId teamId, String name){
        return ChatRoom.builder()
                .chatRoomId(ChatRoomId.newId())
                .teamId(teamId)
                .name(name)
                .build();
    }

    public void rename(String name){
        this.name = name;
    }
}

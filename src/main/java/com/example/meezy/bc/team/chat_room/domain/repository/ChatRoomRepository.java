package com.example.meezy.bc.team.chat_room.domain.repository;

import com.example.meezy.bc.team.chat_room.domain.ChatRoom;
import com.example.meezy.bc.team.chat_room.domain.vo.ChatRoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, ChatRoomId> {
    List<ChatRoom> findAllByTeamId_Value(UUID teamIdValue);

    Optional<ChatRoom> findByChatRoomId_Value(UUID chatRoomIdValue);
}

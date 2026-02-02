package com.example.meezy.bc.collaboration.chat_room.presentation.controller;

import com.example.meezy.bc.collaboration.chat_room.application.service.CreateChatRoomService;
import com.example.meezy.bc.collaboration.chat_room.application.service.DeleteChatRoomService;
import com.example.meezy.bc.collaboration.chat_room.application.service.QueryChatRoomService;
import com.example.meezy.bc.collaboration.chat_room.application.service.UpdateChatRoomService;
import com.example.meezy.bc.collaboration.chat_room.application.service.dto.request.CreateChatRoomRequest;
import com.example.meezy.bc.collaboration.chat_room.application.service.dto.request.UpdateChatRoomNameRequest;
import com.example.meezy.bc.collaboration.chat_room.application.service.dto.response.ChatRoomResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams/{teamId}/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final CreateChatRoomService createChatRoomService;
    private final QueryChatRoomService queryChatRoomService;
    private final UpdateChatRoomService updateChatRoomService;
    private final DeleteChatRoomService deleteChatRoomService;

    @PostMapping
    public void create(
            @PathVariable UUID teamId,
            @Valid @RequestBody CreateChatRoomRequest request
    ) {
        createChatRoomService.create(teamId, request);
    }

    @GetMapping
    public List<ChatRoomResponse> queryAll(@PathVariable UUID teamId) {
        return queryChatRoomService.queryAll(teamId);
    }

    @PatchMapping("/{chatRoomId}/name")
    public void updateName(
            @PathVariable UUID teamId,
            @PathVariable UUID chatRoomId,
            @Valid @RequestBody UpdateChatRoomNameRequest request
    ) {
        updateChatRoomService.updateName(teamId, chatRoomId, request);
    }

    @DeleteMapping("/{chatRoomId}")
    public void delete(
            @PathVariable UUID teamId,
            @PathVariable UUID chatRoomId
    ) {
        deleteChatRoomService.delete(teamId, chatRoomId);
    }
}

package com.example.meezy.bc.collaboration.meeting.application.service.dto.response;

import lombok.Builder;

@Builder
public record IceServerResponse(String urls, String username, String credential) {
}

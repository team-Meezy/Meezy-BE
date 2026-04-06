package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.config;

import com.example.meezy.bc.collaboration.meeting.application.port.out.IceServerQueryPort;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.IceServerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IceServerQueryAdapter implements IceServerQueryPort {

    private final IceServerProperties iceServerProperties;

    @Override
    public List<IceServerResponse> getIceServers() {
        return iceServerProperties.iceServers().stream()
                .map(server -> IceServerResponse.builder()
                        .urls(server.urls())
                        .username(server.username())
                        .credential(server.credential())
                        .build())
                .toList();
    }
}

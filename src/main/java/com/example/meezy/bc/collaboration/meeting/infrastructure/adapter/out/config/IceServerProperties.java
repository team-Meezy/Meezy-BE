package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "webrtc")
public record IceServerProperties(List<IceServer> iceServers) {

    public record IceServer(String urls, String username, String credential) {}
}

package com.example.meezy.bc.collaboration.meeting.application.port.out;

import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.IceServerResponse;

import java.util.List;

public interface IceServerQueryPort {

    List<IceServerResponse> getIceServers();
}

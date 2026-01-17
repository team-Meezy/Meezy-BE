package com.example.meezy.bc.team.meeting.application.service.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SignalMessage.Offer.class, name = "offer"),
        @JsonSubTypes.Type(value = SignalMessage.Answer.class, name = "answer"),
        @JsonSubTypes.Type(value = SignalMessage.IceCandidate.class, name = "ice-candidate")
})
public sealed interface SignalMessage {

    UUID fromUserId();
    UUID toUserId();

    record Offer(
            UUID fromUserId,
            UUID toUserId,
            String sdp
    ) implements SignalMessage {}

    record Answer(
            UUID fromUserId,
            UUID toUserId,
            String sdp
    ) implements SignalMessage {}

    record IceCandidate(
            UUID fromUserId,
            UUID toUserId,
            String candidate, //연결 가능한 실제 주소
            String sdpMid, //해당 주소로 보낼 수 있는 미디어 트랙
            Integer sdpMLineIndex //SDP 내애서는 어디 위치에 존재하는가
    ) implements SignalMessage {}
}

package simulations;

import simulations.config.SimulationConfig;
import simulations.support.AuthHelper;
import simulations.support.StompFrameHelper;
import simulations.support.TestEnvironmentHelper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * WebRTC 시그널링 부하 테스트 (STOMP over WebSocket).
 *
 * 측정 대상:
 * - RelaySignalService.relay(): 시그널 메시지마다 DB 2회 조회
 *   1) teamRepository.existsMemberByTeamIdAndUserId()
 *   2) meetingRepository.existsByTeamIdAndStatus()
 * - ICE candidate는 참가자 쌍당 초당 수십 건 발생 → DB 부하 집중
 *
 * 실행:
 * ./gradlew gatlingRun-simulations.SignalingSimulation \
 *   -DbaseUrl=http://localhost:8080 -DwsBaseUrl=ws://localhost:8080 \
 *   -Dusers=10 -DmessagesPerUser=100 -DteamId=<UUID> \
 *   -DfromUserId=<UUID> -DtoUserId=<UUID> \
 *   -DaccountId=testuser01 -Dpassword=pass123!
 */
public class SignalingSimulation extends Simulation {

    private final String baseUrl = SimulationConfig.baseUrl();
    private final String wsBaseUrl = SimulationConfig.wsBaseUrl();
    private final int users = SimulationConfig.users();
    private final int duration = SimulationConfig.durationSeconds();
    private final int rampUp = SimulationConfig.rampUpSeconds();
    private final int messagesPerUser = Integer.getInteger("messagesPerUser", 50);

    private final String wsEndpoint = System.getProperty("wsEndpoint", "/ws-chat");
    private final String jwtHeader = System.getProperty("jwtHeader", "Authorization");
    private final String jwtPrefix = System.getProperty("jwtPrefix", "Bearer ");

    // fromUserId는 인증된 사용자와 일치해야 함 (RelaySignalService.validateSenderIdentity)
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(baseUrl)
        .acceptHeader("application/json")
        .wsBaseUrl(wsBaseUrl);

    // Step 1: REST 로그인으로 JWT 획득
    ChainBuilder authenticate = AuthHelper.login(
        SimulationConfig.accountId(), SimulationConfig.password()
    );

    // Step 2: WebSocket 연결 + STOMP CONNECT
    ChainBuilder wsConnect = exec(
        ws("WS Connect")
            .connect(wsEndpoint + "/websocket")
    ).exec(
        ws("STOMP CONNECT")
            .sendText(session ->
                StompFrameHelper.connectFrame(
                    jwtHeader,
                    jwtPrefix + session.getString("accessToken")
                )
            )
            .await(Duration.ofSeconds(5)).on(
                ws.checkTextMessage("STOMP CONNECTED")
                    .check(regex("CONNECTED"))
            )
    );

    // Step 3: ICE candidate 메시지 반복 전송
    ChainBuilder sendSignals = repeat(messagesPerUser, "i").on(
        exec(session -> {
            String payload = String.format(
                "{\"type\":\"ice-candidate\","
                + "\"fromUserId\":\"%s\","
                + "\"toUserId\":\"%s\","
                + "\"candidate\":\"candidate:842163049 1 udp 1677729535 203.0.113.1 50000 typ srflx\","
                + "\"sdpMid\":\"0\","
                + "\"sdpMLineIndex\":0}",
                session.getString("fromUserId"), session.getString("toUserId")
            );
            return session.set("signalPayload", payload);
        }).exec(
            ws("Send ICE Candidate")
                .sendText(session ->
                    StompFrameHelper.sendFrame(
                        "/app/teams/" + session.getString("teamId") + "/meeting/signal",
                        session.getString("signalPayload")
                    )
                )
        ).pause(Duration.ofMillis(50))
    );

    // Step 4: 연결 종료
    ChainBuilder wsDisconnect = exec(
        ws("STOMP DISCONNECT")
            .sendText(StompFrameHelper.disconnectFrame())
    ).exec(
        ws("WS Close").close()
    );

    ScenarioBuilder scenario = scenario("WebRTC Signaling Load")
        .exec(authenticate)
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.seedProvidedIds())
        .exec(TestEnvironmentHelper.ensureTeam())
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.ensureMeeting())
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper::ensureSignalUserIds)
        .exec(wsConnect)
        .exec(sendSignals)
        .exec(wsDisconnect);

    {
        setUp(
            scenario.injectOpen(
                rampUsers(users).during(rampUp)
            )
        ).protocols(httpProtocol)
         .maxDuration(duration + 30)
         .assertions(
             global().successfulRequests().percent().gt(90.0)
         );
    }
}

package simulations;

import simulations.config.SimulationConfig;
import simulations.support.AuthHelper;
import simulations.support.TestEnvironmentHelper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * 녹음 업로드 Baseline 측정.
 *
 * 측정 대상:
 * - ReceiveRecordingService: S3 업로드가 @Transactional 안에서 실행되는 응답 지연
 * - 동시 업로드 시 DB 커넥션 풀 경합
 *
 * 실행:
 * ./gradlew gatlingRun-simulations.RecordingUploadSimulation \
 *   -DbaseUrl=http://localhost:8080 -Dusers=20 -DdurationSeconds=60 \
 *   -DteamId=<UUID> -DmeetingId=<UUID> -DaccountId=testuser01 -Dpassword=pass123!
 */
public class RecordingUploadSimulation extends Simulation {

    private final String baseUrl = SimulationConfig.baseUrl();
    private final int users = SimulationConfig.users();
    private final int duration = SimulationConfig.durationSeconds();
    private final int rampUp = SimulationConfig.rampUpSeconds();
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(baseUrl)
        .acceptHeader("application/json");

    ChainBuilder authenticate = AuthHelper.login(
        SimulationConfig.accountId(), SimulationConfig.password()
    );

    ChainBuilder uploadRecording = exec(
        http("Upload Recording")
            .post("/teams/#{teamId}/meetings/#{meetingId}/recording")
            .header("Authorization", "Bearer #{accessToken}")
            .bodyPart(
                RawFileBodyPart("file", "test-recording.mp3")
                    .contentType("audio/mpeg")
                    .fileName("test-recording.mp3")
            ).asMultipartForm()
            .check(status().is(202))
    );

    ScenarioBuilder scenario = scenario("Recording Upload Baseline")
        .exec(authenticate)
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.seedProvidedIds())
        .exec(TestEnvironmentHelper.ensureTeam())
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.ensureMeeting())
        .exitHereIfFailed()
        .exec(uploadRecording);

    {
        setUp(
            scenario.injectOpen(
                rampUsers(users).during(rampUp),
                constantUsersPerSec(2).during(duration)
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().percentile3().lt(5000),
             global().successfulRequests().percent().gt(95.0)
         );
    }
}

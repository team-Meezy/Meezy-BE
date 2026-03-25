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
 * Report Generation 스트레스 테스트.
 *
 * 측정 대상:
 * - MeetingAnalyzerAdapter.transcribe(): 오디오 전체 힙 로드 + 순차 Whisper 처리
 * - ReportGenerator.generate(): @Transactional 안에서 CompletableFuture.join() 블로킹 → DB 커넥션 점유
 * - 다수 동시 업로드 시 async report 생성이 DB 커넥션 풀을 고갈시키는지 확인
 *
 * 전략:
 * - Phase A: 동시 업로드 폭탄 → async report 생성 폭주 유발
 * - Phase B: 동시에 헬스체크 API 호출 → 일반 API 응답 저하 측정
 *
 * 실행:
 * ./gradlew gatlingRun-simulations.ReportGenerationStressSimulation \
 *   -DburstUsers=30 -DteamId=<UUID> -DmeetingId=<UUID> \
 *   -DaccountId=testuser01 -Dpassword=pass123!
 */
public class ReportGenerationStressSimulation extends Simulation {

    private final String baseUrl = SimulationConfig.baseUrl();
    private final int burstUsers = Integer.getInteger("burstUsers", 30);

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(baseUrl)
        .acceptHeader("application/json");

    ChainBuilder authenticate = AuthHelper.login(
        SimulationConfig.accountId(), SimulationConfig.password()
    );

    ChainBuilder uploadRecording = exec(
        http("Stress Upload Recording")
            .post("/teams/#{teamId}/meetings/#{meetingId}/recording")
            .header("Authorization", "Bearer #{accessToken}")
            .bodyPart(
                RawFileBodyPart("file", "test-recording.mp3")
                    .contentType("audio/mpeg")
                    .fileName("test-recording.mp3")
            ).asMultipartForm()
            .check(status().in(202, 500))
    );

    ChainBuilder healthCheck = exec(
        http("Health Check - Active Meeting")
            .get("/teams/#{teamId}/meetings/active")
            .header("Authorization", "Bearer #{accessToken}")
            .check(status().in(200, 204))
    );

    // Phase A: 동시 업로드 → async report 생성 폭주
    ScenarioBuilder uploadBurst = scenario("Upload Burst")
        .exec(authenticate)
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.seedProvidedIds())
        .exec(TestEnvironmentHelper.ensureTeam())
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.ensureMeeting())
        .exitHereIfFailed()
        .exec(uploadRecording);

    // Phase B: 일반 API 응답성 모니터링
    ScenarioBuilder healthProbes = scenario("Health Probes During Stress")
        .exec(authenticate)
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.seedProvidedIds())
        .exec(TestEnvironmentHelper.ensureTeam())
        .exitHereIfFailed()
        .exec(TestEnvironmentHelper.ensureMeeting())
        .exitHereIfFailed()
        .pause(5)
        .repeat(20).on(
            exec(healthCheck).pause(1)
        );

    {
        setUp(
            uploadBurst.injectOpen(
                atOnceUsers(burstUsers)
            ),
            healthProbes.injectOpen(
                rampUsers(10).during(5)
            )
        ).protocols(httpProtocol)
         .assertions(
             forAll().responseTime().percentile3().lt(10000),
             details("Health Check - Active Meeting")
                 .responseTime().percentile3().lt(3000)
         );
    }
}

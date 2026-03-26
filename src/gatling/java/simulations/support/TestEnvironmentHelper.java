package simulations.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;
import simulations.config.SimulationConfig;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public final class TestEnvironmentHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEAM_IMAGE_RESOURCE = "test-team-image.png";

    private TestEnvironmentHelper() {}

    public static ChainBuilder requireSessionAttribute(String attribute, String flag) {
        return exec(session -> {
            if (!session.contains(attribute)
                    || session.getString(attribute) == null
                    || session.getString(attribute).isBlank()) {
                System.err.println("[FAIL-FAST] Session attribute '" + attribute
                    + "' is missing or blank. Provide " + flag + " system property.");
                return session.markAsFailed();
            }
            return session;
        });
    }

    public static ChainBuilder seedProvidedIds() {
        return exec(session -> {
            Session current = session;

            if (SimulationConfig.teamId() != null) {
                current = current.set("teamId", SimulationConfig.teamId());
            }
            if (SimulationConfig.meetingId() != null) {
                current = current.set("meetingId", SimulationConfig.meetingId());
            }
            if (SimulationConfig.fromUserId() != null) {
                current = current.set("fromUserId", SimulationConfig.fromUserId());
            }
            if (SimulationConfig.toUserId() != null) {
                current = current.set("toUserId", SimulationConfig.toUserId());
            }

            return current;
        });
    }

    public static ChainBuilder ensureTeam() {
        ChainBuilder createTeam = exec(session ->
            session.set("teamName", "gatling-team-" + UUID.randomUUID())
        ).exec(
            http("Create Team")
                .post("/teams")
                .header("Authorization", "Bearer #{accessToken}")
                .bodyPart(StringBodyPart("name", "#{teamName}"))
                .bodyPart(
                    RawFileBodyPart("serverImage", TEAM_IMAGE_RESOURCE)
                        .contentType("image/png")
                        .fileName("test-team-image.png")
                )
                .asMultipartForm()
                .check(status().is(200))
        ).exec(
            http("Query Teams")
                .get("/teams")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().is(200))
                .check(bodyString().saveAs("teamsResponse"))
        ).exec(TestEnvironmentHelper::extractCreatedTeamId);

        return doIf(session -> !session.contains("teamId")).then(createTeam);
    }

    public static ChainBuilder ensureMeeting() {
        ChainBuilder fetchOrCreateMeeting = exec(
            http("Get Active Meeting")
                .get("/teams/#{teamId}/meetings/active")
                .header("Authorization", "Bearer #{accessToken}")
                .check(status().saveAs("activeMeetingStatus"))
                .check(bodyString().optional().saveAs("activeMeetingBody"))
        ).exec(
            doIf(session -> session.getInt("activeMeetingStatus") == 200).then(
                exec(TestEnvironmentHelper::extractActiveMeeting)
            )
        ).exec(
            doIf(session -> session.getInt("activeMeetingStatus") != 200).then(
                exec(
                    http("Start Meeting")
                        .post("/teams/#{teamId}/meetings")
                        .header("Authorization", "Bearer #{accessToken}")
                        .check(status().is(200))
                        .check(jsonPath("$.meetingId").saveAs("meetingId"))
                        .check(jsonPath("$.hostUserId").saveAs("hostUserId"))
                )
            )
        );

        return doIf(session -> !session.contains("meetingId")).then(fetchOrCreateMeeting);
    }

    public static Session captureUserIdFromToken(Session session) {
        String accessToken = session.getString("accessToken");

        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("[FAIL-FAST] accessToken is missing or blank.");
            return session.markAsFailed();
        }

        String[] parts = accessToken.split("\\.");

        if (parts.length < 2) {
            System.err.println("[FAIL-FAST] Invalid JWT format: expected 3 parts, got " + parts.length);
            return session.markAsFailed();
        }

        try {
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<>() {});
            return session.set("currentUserId", payload.get("sub").toString());
        } catch (Exception e) {
            System.err.println("[FAIL-FAST] Failed to decode JWT subject: " + e.getMessage());
            return session.markAsFailed();
        }
    }

    public static Session ensureSignalUserIds(Session session) {
        Session current = session;
        String currentUserId = current.getString("currentUserId");

        if (!current.contains("fromUserId")) {
            current = current.set("fromUserId", currentUserId);
        }

        if (!current.contains("toUserId")) {
            current = current.set("toUserId", currentUserId);
        }

        return current;
    }

    private static Session extractCreatedTeamId(Session session) {
        String teamsResponse = session.getString("teamsResponse");
        String teamName = session.getString("teamName");

        try {
            List<Map<String, Object>> teams = OBJECT_MAPPER.readValue(teamsResponse, new TypeReference<>() {});

            return teams.stream()
                .filter(team -> teamName.equals(team.get("teamName")))
                .findFirst()
                .map(team -> session.set("teamId", team.get("teamId").toString()))
                .orElseThrow(() -> new IllegalStateException("Created team not found in /teams response"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve created team id", e);
        }
    }

    private static Session extractActiveMeeting(Session session) {
        String activeMeetingBody = session.getString("activeMeetingBody");

        try {
            Map<String, Object> meeting = OBJECT_MAPPER.readValue(activeMeetingBody, new TypeReference<>() {});
            return session
                .set("meetingId", meeting.get("meetingId").toString())
                .set("hostUserId", meeting.get("hostUserId").toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse active meeting response", e);
        }
    }
}

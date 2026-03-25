package simulations.support;

import io.gatling.javaapi.core.ChainBuilder;
import simulations.config.SimulationConfig;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public final class AuthHelper {

    private AuthHelper() {}

    public static ChainBuilder login(String accountId, String password) {
        String providedAccessToken = SimulationConfig.accessToken();

        if (providedAccessToken != null && !providedAccessToken.isBlank()) {
            return exec(session -> session.set("accessToken", providedAccessToken))
                .exec(TestEnvironmentHelper::captureUserIdFromToken);
        }

        return exec(
            http("Login")
                .post("/auth/login")
                .header("Content-Type", "application/json")
                .body(StringBody(
                    "{\"accountId\":\"" + accountId + "\",\"password\":\"" + password + "\"}"
                ))
                .check(status().is(200))
                .check(jsonPath("$.accessToken").saveAs("accessToken"))
        ).exec(
            doIf(session -> session.contains("accessToken")).then(
                exec(TestEnvironmentHelper::captureUserIdFromToken)
            )
        );
    }
}

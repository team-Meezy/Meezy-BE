package simulations.config;

public final class SimulationConfig {

    private SimulationConfig() {}

    public static String baseUrl() {
        return System.getProperty("baseUrl", "http://localhost:8080");
    }

    public static String wsBaseUrl() {
        return System.getProperty("wsBaseUrl", "ws://localhost:8080");
    }

    public static int users() {
        return Integer.getInteger("users", 10);
    }

    public static int durationSeconds() {
        return Integer.getInteger("durationSeconds", 60);
    }

    public static int rampUpSeconds() {
        return Integer.getInteger("rampUpSeconds", 10);
    }

    public static String accountId() {
        return System.getProperty("accountId", "testuser01");
    }

    public static String password() {
        return System.getProperty("password", "pass123!");
    }

    public static String accessToken() {
        return System.getProperty("accessToken");
    }

    public static String teamId() {
        return System.getProperty("teamId");
    }

    public static String meetingId() {
        return System.getProperty("meetingId");
    }

    public static String fromUserId() {
        return System.getProperty("fromUserId");
    }

    public static String toUserId() {
        return System.getProperty("toUserId");
    }
}

package simulations.support;

public final class StompFrameHelper {

    private static final String NULL_CHAR = "\u0000";
    private static final String LF = "\n";

    private StompFrameHelper() {}

    public static String connectFrame(String authHeaderName, String tokenWithPrefix) {
        return "CONNECT" + LF
             + "accept-version:1.2" + LF
             + "heart-beat:0,0" + LF
             + authHeaderName + ":" + tokenWithPrefix + LF
             + LF + NULL_CHAR;
    }

    public static String subscribeFrame(String destination, String subscriptionId) {
        return "SUBSCRIBE" + LF
             + "id:" + subscriptionId + LF
             + "destination:" + destination + LF
             + LF + NULL_CHAR;
    }

    public static String sendFrame(String destination, String jsonBody) {
        return "SEND" + LF
             + "destination:" + destination + LF
             + "content-type:application/json" + LF
             + LF + jsonBody + NULL_CHAR;
    }

    public static String disconnectFrame() {
        return "DISCONNECT" + LF
             + LF + NULL_CHAR;
    }
}

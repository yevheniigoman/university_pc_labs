public enum MessageTypes {
    SETUP(1),
    SUCCESS(2),
    ERROR(3),
    PROCESS(4),
    STATUS(5),
    GET_RESULT(6),
    SEND_RESULT(7);

    private final int code;

    MessageTypes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    static MessageTypes fromInt(int code) throws IllegalArgumentException {
        return switch (code) {
            case 1 -> SETUP;
            case 2 -> SUCCESS;
            case 3 -> ERROR;
            case 4 -> PROCESS;
            case 5 -> STATUS;
            case 6 -> GET_RESULT;
            case 7 -> SEND_RESULT;
            default -> throw new IllegalArgumentException("Invalid message code " + code);
        };
    }
}
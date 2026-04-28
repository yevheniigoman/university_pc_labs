public enum StatusCodes {
    IN_PROGRESS(1),
    FAILED(2),
    NOT_STARTED(3);

    private final int code;

    StatusCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

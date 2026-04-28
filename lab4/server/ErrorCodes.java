public enum ErrorCodes {
    INVALID_TYPE(0),
    INVALID_NUM_THREADS(1),
    INVALID_MATRIX_DIM(2),
    SETUP_REQUIRED(3);

    private final int code;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

package co.com.pragma.model.customExceptions;

public class InvalidPaginationException extends BusinessException {

    private static final String ERROR_CODE = "LST-001";

    public InvalidPaginationException(String specificMessage) {
        super(ERROR_CODE, specificMessage);
    }
}
package co.com.pragma.model.customExceptions;

public class InvalidLoanApplicationException extends BusinessException {

    private static final String ERROR_CODE = "SOL-002";

    public InvalidLoanApplicationException(String specificMessage) {
        super(ERROR_CODE, specificMessage);
    }
}
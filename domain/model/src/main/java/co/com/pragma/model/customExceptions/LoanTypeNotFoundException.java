package co.com.pragma.model.customExceptions;

public class LoanTypeNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "SOL-001";

    public LoanTypeNotFoundException(String loanTypeId) {
        super(ERROR_CODE, "El tipo de préstamo con ID '" + loanTypeId + "' no existe.");
    }
}
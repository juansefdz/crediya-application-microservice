package co.com.pragma.model.customExceptions;

public class LoanApplicationNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "UPD-001";
    public LoanApplicationNotFoundException(String id) {
        super(ERROR_CODE, "La solicitud de préstamo con id '" + id + "' no fue encontrada.");
    }
}
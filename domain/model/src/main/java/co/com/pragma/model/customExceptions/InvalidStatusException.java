package co.com.pragma.model.customExceptions;

public class InvalidStatusException extends BusinessException {
    private static final String ERROR_CODE = "UPD-002";
    public InvalidStatusException(String attemptedStatus) {
        super(ERROR_CODE, "El estado '" + attemptedStatus + "' no es un estado válido para esta operación.");
    }
}
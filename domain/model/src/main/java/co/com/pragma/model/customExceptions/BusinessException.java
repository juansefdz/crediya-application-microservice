package co.com.pragma.model.customExceptions;

import co.com.pragma.model.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        super(null, null, false, false); //para que no llegue mensaje plano sino en la estructura
        this.errorCode = errorCode;
        this.detail = errorCode.getMessage();
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(null, null, false, false);
        this.errorCode = errorCode;
        this.detail = String.format(errorCode.getMessage(), args);
    }

    public String getCode() {
        return errorCode.getCode();
    }

    @Override
    public String getMessage() {
        return detail;
    }
}

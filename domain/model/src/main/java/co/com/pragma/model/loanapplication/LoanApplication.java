package co.com.pragma.model.loanapplication;
import co.com.pragma.model.LoanApplicationStatus;
import lombok.*;


import java.math.BigDecimal;

@Builder(toBuilder = true)
@Getter
@ToString
public class LoanApplication {
    private String id;
    private BigDecimal monto;
    private Integer plazo;
    private String email;

    private String usuarioId;
    private String prestamoId;
    private LoanApplicationStatus status;
}
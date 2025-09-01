package co.com.pragma.model.loanapplication;
import co.com.pragma.model.LoanApplicationStatus;
import lombok.*;
//import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder(toBuilder = true)
@Getter
public class LoanApplication {
    private String id;
    private BigDecimal monto;
    private Integer plazo;
    private String usuarioId;
    private String prestamoId;
    private Integer estadoId;
    private LoanApplicationStatus status;
}
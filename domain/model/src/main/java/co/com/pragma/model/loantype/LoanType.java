package co.com.pragma.model.loantype;
import lombok.*;
//import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class LoanType {

    private String id;
    private String nombre;


    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;

    private Double tasaInteres;
    private Boolean validacionAutomatica;
}
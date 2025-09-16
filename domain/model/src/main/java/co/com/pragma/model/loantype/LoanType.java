package co.com.pragma.model.loantype;
import lombok.*;

import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
public class LoanType {
    String id;
    String nombre;
    BigDecimal montoMinimo;
    BigDecimal montoMaximo;
    BigDecimal tasaInteres;
    boolean automaticValidation;
}
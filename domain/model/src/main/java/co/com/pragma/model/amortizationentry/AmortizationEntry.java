package co.com.pragma.model.amortizationentry;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AmortizationEntry {
    private final int installmentNumber;
    private final BigDecimal capitalPayment;
    private final BigDecimal interestPayment;
    private final BigDecimal outstandingBalance;
}

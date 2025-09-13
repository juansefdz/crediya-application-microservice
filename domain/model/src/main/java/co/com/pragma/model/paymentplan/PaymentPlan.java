package co.com.pragma.model.paymentplan;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import co.com.pragma.model.amortizationentry.AmortizationEntry;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PaymentPlan {
    private final List<AmortizationEntry> entries;
    private final BigDecimal totalInterest;
}
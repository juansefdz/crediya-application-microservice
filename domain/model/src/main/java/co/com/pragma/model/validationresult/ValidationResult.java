package co.com.pragma.model.validationresult;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.paymentplan.PaymentPlan;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResult {
    private final LoanApplicationStatus newStatus;
    private final PaymentPlan paymentPlan;
}
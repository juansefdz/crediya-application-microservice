package co.com.pragma.model.financial.gateways;

import co.com.pragma.model.paymentplan.PaymentPlan;
import java.math.BigDecimal;

public interface FinancialCalculatorGateway {

    BigDecimal calculateMonthlyPayment(BigDecimal principal, int termInMonths, BigDecimal monthlyInterestRate);

    PaymentPlan generatePaymentPlan(BigDecimal principal, int termInMonths, BigDecimal monthlyInterestRate);
}
package co.com.pragma.lambda.debtcapacoty;

import co.com.pragma.model.amortizationentry.AmortizationEntry;
import co.com.pragma.model.financial.gateways.FinancialCalculatorGateway;
import co.com.pragma.model.paymentplan.PaymentPlan;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class FinancialCalculatorAdapter implements FinancialCalculatorGateway {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    @Override
    public BigDecimal calculateMonthlyPayment(BigDecimal principal, int termInMonths, BigDecimal monthlyInterestRate) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) == 0 || termInMonths == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal i = monthlyInterestRate;
        BigDecimal onePlusIPowN = (BigDecimal.ONE.add(i)).pow(termInMonths, MC);
        BigDecimal numerator = principal.multiply(i.multiply(onePlusIPowN, MC), MC);
        BigDecimal denominator = onePlusIPowN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return principal;
        }

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    public PaymentPlan generatePaymentPlan(BigDecimal principal, int termInMonths, BigDecimal monthlyInterestRate) {
        List<AmortizationEntry> entries = new ArrayList<>();
        BigDecimal outstandingBalance = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, termInMonths, monthlyInterestRate);

        for (int i = 1; i <= termInMonths; i++) {
            BigDecimal interestPayment = outstandingBalance.multiply(monthlyInterestRate, MC).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capitalPayment = monthlyPayment.subtract(interestPayment);
            outstandingBalance = outstandingBalance.subtract(capitalPayment);

            if (i == termInMonths && outstandingBalance.compareTo(BigDecimal.ZERO) != 0) {
                capitalPayment = capitalPayment.add(outstandingBalance);
                outstandingBalance = BigDecimal.ZERO;
            }

            entries.add(AmortizationEntry.builder()
                    .installmentNumber(i)
                    .capitalPayment(capitalPayment)
                    .interestPayment(interestPayment)
                    .outstandingBalance(outstandingBalance)
                    .build());
            totalInterest = totalInterest.add(interestPayment);
        }

        return PaymentPlan.builder()
                .entries(entries)
                .totalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
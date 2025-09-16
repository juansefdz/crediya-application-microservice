package co.com.pragma.lambda.debtcapacoty;

import co.com.pragma.model.financial.gateways.BusinessRulesGateway;
import java.math.BigDecimal;

public class BusinessRulesAdapter implements BusinessRulesGateway {

    @Override
    public BigDecimal getDebtCapacityRatio() {
        return new BigDecimal("0.35"); // 35%
    }

    @Override
    public BigDecimal getMonthlyInterestRate() {
        return new BigDecimal("0.015"); // 1.5% mensual
    }

    @Override
    public BigDecimal getFiveSalariesMultiplier() {
        return new BigDecimal("5");
    }

    @Override
    public BigDecimal getMinIncomeRequired() {
        return new BigDecimal("1300000.00");
    }

    @Override
    public BigDecimal getMaxDebtRatio() {
        return new BigDecimal("0.70");
    }
}
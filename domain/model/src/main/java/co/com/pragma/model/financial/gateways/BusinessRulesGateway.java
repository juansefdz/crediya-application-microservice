package co.com.pragma.model.financial.gateways;

import java.math.BigDecimal;

public interface BusinessRulesGateway {

    BigDecimal getDebtCapacityRatio();

    BigDecimal getMonthlyInterestRate();

    BigDecimal getFiveSalariesMultiplier();

    BigDecimal getMinIncomeRequired();

    BigDecimal getMaxDebtRatio();
}
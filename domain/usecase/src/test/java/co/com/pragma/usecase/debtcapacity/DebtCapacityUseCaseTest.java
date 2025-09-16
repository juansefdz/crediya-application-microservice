package co.com.pragma.usecase.debtcapacity;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.financial.gateways.BusinessRulesGateway;
import co.com.pragma.model.financial.gateways.FinancialCalculatorGateway;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.paymentplan.PaymentPlan;
import co.com.pragma.model.userdata.UserData;
import co.com.pragma.model.userdata.gateways.UserDataGateWay;
import co.com.pragma.model.validationresult.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtCapacityUseCaseTest {

    @Mock private LoanApplicationRepository loanApplicationRepository;
    @Mock private UserDataGateWay userDataGateway;
    @Mock private FinancialCalculatorGateway financialCalculator;
    @Mock private BusinessRulesGateway rules;

    @InjectMocks
    private DebtCapacityUseCase debtCapacityUseCase;

    private LoanApplication applicationToValidate;
    private UserData userData;

    @BeforeEach
    void setUp() {
        applicationToValidate = LoanApplication.builder()
                .id("new-loan-123")
                .usuarioId("user-abc")
                .monto(new BigDecimal("10000000"))
                .plazo(36)
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .build();

        userData = UserData.builder()
                .id("user-abc")
                .totalIncome(new BigDecimal("5000000"))
                .build();
    }

    @Test
    @DisplayName("Debe APROBAR el préstamo cuando la capacidad de endeudamiento es suficiente")
    void shouldApproveLoanWhenCapacityIsSufficient() {

        when(rules.getDebtCapacityRatio()).thenReturn(new BigDecimal("0.35"));
        when(rules.getMonthlyInterestRate()).thenReturn(new BigDecimal("0.015"));
        when(rules.getFiveSalariesMultiplier()).thenReturn(new BigDecimal("5"));
        when(rules.getMinIncomeRequired()).thenReturn(new BigDecimal("1300000"));
        when(rules.getMaxDebtRatio()).thenReturn(new BigDecimal("0.70"));


        BigDecimal newLoanPayment = new BigDecimal("416037.00");
        BigDecimal existingLoanPayment = new BigDecimal("208018.00");


        when(financialCalculator.calculateMonthlyPayment(eq(new BigDecimal("10000000")), eq(36), any(BigDecimal.class)))
                .thenReturn(newLoanPayment);
        when(financialCalculator.calculateMonthlyPayment(eq(new BigDecimal("5000000")), eq(36), any(BigDecimal.class)))
                .thenReturn(existingLoanPayment);

        when(financialCalculator.generatePaymentPlan(any(), anyInt(), any()))
                .thenReturn(PaymentPlan.builder().entries(Collections.nCopies(36, null)).build());


        LoanApplication existingLoan = LoanApplication.builder()
                .monto(new BigDecimal("5000000")).plazo(36).status(LoanApplicationStatus.APROBADA).build();

        when(loanApplicationRepository.findById("new-loan-123")).thenReturn(Mono.just(applicationToValidate));
        when(userDataGateway.findUserFinancialDataById("user-abc")).thenReturn(Mono.just(userData));
        when(loanApplicationRepository.findByUserIdAndStatus("user-abc", LoanApplicationStatus.APROBADA))
                .thenReturn(Flux.just(existingLoan));


        Mono<ValidationResult> result = debtCapacityUseCase.execute("new-loan-123");


        StepVerifier.create(result)
                .assertNext(validationResult -> {
                    assertThat(validationResult.getNewStatus()).isEqualTo(LoanApplicationStatus.APROBADA);
                    assertThat(validationResult.getPaymentPlan()).isNotNull();
                    assertThat(validationResult.getPaymentPlan().getEntries()).hasSize(36);
                })
                .verifyComplete();
    }
}
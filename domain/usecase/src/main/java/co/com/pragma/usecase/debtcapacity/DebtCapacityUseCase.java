package co.com.pragma.usecase.debtcapacity;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.financial.gateways.BusinessRulesGateway;
import co.com.pragma.model.financial.gateways.FinancialCalculatorGateway;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.paymentplan.PaymentPlan;
import co.com.pragma.model.userdata.UserData;
import co.com.pragma.model.userdata.gateways.UserDataGateWay;
import co.com.pragma.model.validationresult.ValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Slf4j
@RequiredArgsConstructor
public class DebtCapacityUseCase {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserDataGateWay userDataGateWay;
    private final FinancialCalculatorGateway financialCalculator;
    private final BusinessRulesGateway rules;


    private record ValidationContext(LoanApplication application, UserData userData, BigDecimal currentDebt) {}

    public Mono<ValidationResult> execute(String loanApplicationId) {
        log.info("Iniciando cálculo de capacidad para solicitud ID: {}", loanApplicationId);

        if (loanApplicationId == null || loanApplicationId.trim().isEmpty()) {
            return Mono.error(new BusinessException(ErrorCode.VAL_USER_ID_REQUIRED));
        }

        return findApplication(loanApplicationId)
                .flatMap(this::buildValidationContext)
                .flatMap(this::applyBusinessValidations)
                .flatMap(this::makeDecisionAndBuildResult)
                .doOnError(error -> log.error("Error en el flujo de validación: {}", error.getMessage()));
    }


    private Mono<LoanApplication> findApplication(String loanApplicationId) {
        return loanApplicationRepository.findById(loanApplicationId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.APP_NOT_FOUND, loanApplicationId)))
                .filter(app -> app.getStatus() == LoanApplicationStatus.PENDIENTE_REVISION)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.VAL_STATUS_INVALID, LoanApplicationStatus.PENDIENTE_REVISION.name())));
    }


    private Mono<ValidationContext> buildValidationContext(LoanApplication application) {
        Mono<UserData> userDataMono = userDataGateWay.findUserFinancialDataById(application.getUsuarioId())
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_DATA_NOT_FOUND, application.getUsuarioId())));

        Mono<BigDecimal> currentDebtMono = calculateCurrentMonthlyDebt(application.getUsuarioId());

        return Mono.zip(userDataMono, currentDebtMono)
                .map(tuple -> new ValidationContext(application, tuple.getT1(), tuple.getT2()));
    }


    private Mono<ValidationContext> applyBusinessValidations(ValidationContext context) {
        return Mono.just(context)
                .flatMap(this::validateUserIncome)
                .flatMap(this::validateCurrentDebtRatio)
                .flatMap(this::validateNewDebtCapacity);
    }


    private Mono<ValidationResult> makeDecisionAndBuildResult(ValidationContext context) {
        BigDecimal newLoanPayment = financialCalculator.calculateMonthlyPayment(
                context.application.getMonto(),
                context.application.getPlazo(),
                rules.getMonthlyInterestRate()
        );

        BigDecimal maxAllowedDebt = context.userData.getTotalIncome().multiply(rules.getDebtCapacityRatio(), MC);
        BigDecimal availableCapacity = maxAllowedDebt.subtract(context.currentDebt);

        if (newLoanPayment.compareTo(availableCapacity) <= 0) {
            return approveApplication(context);
        } else {
            return rejectApplication();
        }
    }

    //aprobación  o rechazo

    private Mono<ValidationResult> approveApplication(ValidationContext context) {
        BigDecimal fiveSalaries = context.userData.getTotalIncome().multiply(rules.getFiveSalariesMultiplier(), MC);
        LoanApplicationStatus finalStatus = LoanApplicationStatus.APROBADA;

        if (context.application.getMonto().compareTo(fiveSalaries) > 0) {
            log.warn("⚠Solicitud {} requiere revisión manual. Monto: {} > Límite: {}",
                    context.application.getId(), context.application.getMonto(), fiveSalaries);
            finalStatus = LoanApplicationStatus.REVISION_MANUAL;
        }

        try {
            PaymentPlan paymentPlan = financialCalculator.generatePaymentPlan(
                    context.application.getMonto(),
                    context.application.getPlazo(),
                    rules.getMonthlyInterestRate()
            );
            return Mono.just(ValidationResult.builder()
                    .newStatus(finalStatus)
                    .paymentPlan(paymentPlan)
                    .build());
        } catch (Exception e) {
            log.error("Error generando plan de pagos para {}", context.application.getId(), e);
            return Mono.error(new BusinessException(ErrorCode.SYS_PLAN_GENERATION_ERROR));
        }
    }

    private Mono<ValidationResult> rejectApplication() {
        return Mono.just(ValidationResult.builder()
                .newStatus(LoanApplicationStatus.RECHAZADA)
                .paymentPlan(null)
                .build());
    }

    //métodos de Validación Específicos

    private Mono<ValidationContext> validateUserIncome(ValidationContext context) {
        if (context.userData.getTotalIncome().compareTo(rules.getMinIncomeRequired()) < 0) {
            return Mono.error(new BusinessException(ErrorCode.VAL_INSUFFICIENT_INCOME));
        }
        return Mono.just(context);
    }

    private Mono<ValidationContext> validateCurrentDebtRatio(ValidationContext context) {
        if (context.userData.getTotalIncome().compareTo(BigDecimal.ZERO) == 0) {
            return Mono.error(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
        BigDecimal currentDebtRatio = context.currentDebt.divide(context.userData.getTotalIncome(), MC);
        if (currentDebtRatio.compareTo(rules.getMaxDebtRatio()) > 0) {
            return Mono.error(new BusinessException(ErrorCode.VAL_EXCESSIVE_DEBT_RATIO));
        }
        return Mono.just(context);
    }

    private Mono<ValidationContext> validateNewDebtCapacity(ValidationContext context) {
        BigDecimal newLoanPayment = financialCalculator.calculateMonthlyPayment(
                context.application.getMonto(), context.application.getPlazo(), rules.getMonthlyInterestRate()
        );
        BigDecimal totalNewDebt = context.currentDebt.add(newLoanPayment);
        BigDecimal maxAllowedDebt = context.userData.getTotalIncome().multiply(rules.getDebtCapacityRatio(), MC);

        if (totalNewDebt.compareTo(maxAllowedDebt) > 0) {

            return Mono.error(new BusinessException(ErrorCode.VAL_INSUFFICIENT_INCOME));
        }
        return Mono.just(context);
    }


    private Mono<BigDecimal> calculateCurrentMonthlyDebt(String userId) {
        return loanApplicationRepository.findByUserIdAndStatus(userId, LoanApplicationStatus.APROBADA)
                .map(loan -> financialCalculator.calculateMonthlyPayment(
                        loan.getMonto(), loan.getPlazo(), rules.getMonthlyInterestRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
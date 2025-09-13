package co.com.pragma.usecase.debtcapacity;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.amortizationentry.AmortizationEntry;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.paymentplan.PaymentPlan;
import co.com.pragma.model.userdata.UserData;
import co.com.pragma.model.userdata.gateways.UserDataRepository;
import co.com.pragma.model.validationresult.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DebtCapacityUseCase {

    private static final BigDecimal DEBT_CAPACITY_RATIO = new BigDecimal("0.35");
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.015");
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal FIVE_SALARIES_MULTIPLIER = new BigDecimal("5");

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserDataRepository userDataRepository;

    public Mono<ValidationResult> execute(String loanApplicationId) {
        log.info("Iniciando cálculo de capacidad para solicitud ID: {}", loanApplicationId);

        return loanApplicationRepository.findById(loanApplicationId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.APP_NOT_FOUND, loanApplicationId)))
                .flatMap(application -> {

                    Mono<UserData> userDataMono = userDataRepository.findUserFinancialDataById(application.getUsuarioId())
                            .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_DATA_NOT_FOUND, application.getUsuarioId())));

                    Mono<BigDecimal> currentDebtMono = calculateCurrentMonthlyDebt(application.getUsuarioId());

                    return Mono.zip(Mono.just(application), userDataMono, currentDebtMono)
                            .flatMap(tuple -> {
                                LoanApplication appToValidate = tuple.getT1();
                                UserData userData = tuple.getT2();
                                BigDecimal currentMonthlyDebt = tuple.getT3();

                                return decideApproval(appToValidate, userData, currentMonthlyDebt);
                            });
                });
    }

    private Mono<ValidationResult> decideApproval(LoanApplication app, UserData user, BigDecimal currentDebt) {
        BigDecimal maxDebtCapacity = user.getTotalIncome().multiply(DEBT_CAPACITY_RATIO, MC);
        BigDecimal availableCapacity = maxDebtCapacity.subtract(currentDebt);
        BigDecimal newLoanPayment = calculateNewLoanMonthlyPayment(app.getMonto(), app.getPlazo());

        log.info("Capacidad disponible: {}, Cuota del nuevo préstamo: {}", availableCapacity, newLoanPayment);

        LoanApplicationStatus finalStatus;
        PaymentPlan paymentPlan = null;

        if (newLoanPayment.compareTo(availableCapacity) <= 0) {
            log.info("Decisión inicial: APROBADO para solicitud ID: {}", app.getId());
            finalStatus = LoanApplicationStatus.APROBADA;

            BigDecimal fiveSalaries = user.getTotalIncome().multiply(FIVE_SALARIES_MULTIPLIER, MC);
            if (app.getMonto().compareTo(fiveSalaries) > 0) {
                log.info("Monto {} supera 5 salarios ({}). Pasa a REVISION_MANUAL.", app.getMonto(), fiveSalaries);
                finalStatus = LoanApplicationStatus.REVISION_MANUAL;
            }
            paymentPlan = generatePaymentPlan(app.getMonto(), app.getPlazo());
        } else {
            log.info("Decisión: RECHAZADO para solicitud ID: {}", app.getId());
            finalStatus = LoanApplicationStatus.RECHAZADA;
        }

        return Mono.just(ValidationResult.builder()
                .newStatus(finalStatus)
                .paymentPlan(paymentPlan)
                .build());
    }

    private Mono<BigDecimal> calculateCurrentMonthlyDebt(String userId) {
        return loanApplicationRepository.findByUserIdAndStatus(userId, LoanApplicationStatus.APROBADA)
                .map(loan -> calculateNewLoanMonthlyPayment(loan.getMonto(), loan.getPlazo()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    private BigDecimal calculateNewLoanMonthlyPayment(BigDecimal principal, int termInMonths) {
        if (principal.compareTo(BigDecimal.ZERO) == 0 || termInMonths == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal i = MONTHLY_INTEREST_RATE;
        BigDecimal onePlusIPowN = (BigDecimal.ONE.add(i)).pow(termInMonths, MC);
        BigDecimal numerator = principal.multiply(i.multiply(onePlusIPowN, MC), MC);
        BigDecimal denominator = onePlusIPowN.subtract(BigDecimal.ONE);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return principal;
        }
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private PaymentPlan generatePaymentPlan(BigDecimal principal, int termInMonths) {
        List<AmortizationEntry> entries = new ArrayList<>();
        BigDecimal outstandingBalance = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal monthlyPayment = calculateNewLoanMonthlyPayment(principal, termInMonths);
        for (int i = 1; i <= termInMonths; i++) {
            BigDecimal interestPayment = outstandingBalance.multiply(MONTHLY_INTEREST_RATE, MC).setScale(2, RoundingMode.HALF_UP);
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
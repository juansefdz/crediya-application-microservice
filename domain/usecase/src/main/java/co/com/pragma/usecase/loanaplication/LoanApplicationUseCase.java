package co.com.pragma.usecase.loanaplication;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<LoanApplication> createLoanApplication(LoanApplication applicationDraft) {
        log.info("Iniciando CU para crear solicitud. usuarioId={}", applicationDraft.getUsuarioId());

        return this.validate(applicationDraft)
                .map(this::prepareForSave)
                .flatMap(loanApplicationRepository::save)
                .doOnSuccess(saved -> log.info("Solicitud creada OK. id={}, usuarioId={}", saved.getId(), saved.getUsuarioId()))
                .doOnError(BusinessException.class, e -> log.warn("Fallo de negocio controlado [{}]: {}", e.getCode(), e.getMessage()))
                .doOnError(e -> !(e instanceof BusinessException), e -> log.error("Error no controlado creando solicitud", e));
    }

    private Mono<Tuple2<LoanApplication, LoanType>> validate(LoanApplication app) {

        return Mono.just(app)
                .flatMap(this::validateInitialData)
                .flatMap(this::validateLoanTypeAndLimits);
    }

    private Mono<LoanApplication> validateInitialData(LoanApplication app) {
        if (app.getMonto() == null || app.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new BusinessException(ErrorCode.VAL_AMOUNT_INVALID));
        }
        if (app.getPlazo() == null || app.getPlazo() <= 0) {
            return Mono.error(new BusinessException(ErrorCode.VAL_TERM_INVALID));
        }
        if (app.getPrestamoId() == null || app.getPrestamoId().isBlank()) {
            return Mono.error(new BusinessException(ErrorCode.VAL_LOAN_TYPE_ID_REQUIRED));
        }
        if (app.getUsuarioId() == null || app.getUsuarioId().isBlank()) {
            return Mono.error(new BusinessException(ErrorCode.VAL_USER_ID_REQUIRED));
        }
        return Mono.just(app);
    }

    private Mono<Tuple2<LoanApplication, LoanType>> validateLoanTypeAndLimits(LoanApplication app) {
        String loanTypeId = app.getPrestamoId();
        return loanTypeRepository.findById(Long.valueOf(loanTypeId))
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.LTY_NOT_FOUND, loanTypeId)))
                .flatMap(loanType -> {
                    if (app.getMonto().compareTo(loanType.getMontoMinimo()) < 0) {
                        return Mono.error(new BusinessException(ErrorCode.VAL_AMOUNT_MIN_EXCEEDED));
                    }
                    if (app.getMonto().compareTo(loanType.getMontoMaximo()) > 0) {
                        return Mono.error(new BusinessException(ErrorCode.VAL_AMOUNT_MAX_EXCEEDED));
                    }
                    return Mono.zip(Mono.just(app), Mono.just(loanType));
                });
    }

    private LoanApplication prepareForSave(Tuple2<LoanApplication, LoanType> tuple) {
        LoanApplication app = tuple.getT1();
        return app.toBuilder()
                .id(UUID.randomUUID().toString())
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .build();
    }
}
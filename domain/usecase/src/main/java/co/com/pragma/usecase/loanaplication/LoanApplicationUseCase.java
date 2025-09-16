package co.com.pragma.usecase.loanaplication;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.sqs.gateways.SqsNotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final SqsNotificationGateway sqsGateway;

    public Mono<LoanApplication> createLoanApplication(LoanApplication applicationDraft) {
        log.info("Iniciando CU para crear solicitud. usuarioId={}", applicationDraft.getUsuarioId());

        return this.validate(applicationDraft)
                .map(this::prepareForSave)
                .flatMap(this::saveAndKeepContext)
                .flatMap(this::sendMessageIfRequired)
                .doOnSuccess(finalApp -> log.info("Proceso de creación finalizado OK. id={}, estado={}", finalApp.getId(), finalApp.getStatus()))
                .doOnError(BusinessException.class, e -> log.warn("Fallo de negocio controlado [{}]: {}", e.getCode(), e.getMessage()));
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
        if (app.getUsuarioId() == null || app.getUsuarioId().isBlank()) {
            return Mono.error(new BusinessException(ErrorCode.VAL_USER_ID_REQUIRED));
        }
        return Mono.just(app);
    }

    private Mono<Tuple2<LoanApplication, LoanType>> validateLoanTypeAndLimits(LoanApplication app) {
        Long loanTypeId = app.getPrestamoId();

        if (loanTypeId == null) {
            return Mono.error(new BusinessException(ErrorCode.VAL_LOAN_TYPE_ID_REQUIRED));
        }

        return loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.LTY_NOT_FOUND, String.valueOf(loanTypeId))))
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
    private Tuple2<LoanApplication, LoanType> prepareForSave(Tuple2<LoanApplication, LoanType> tuple) {
        LoanApplication originalApp = tuple.getT1();


        LoanApplication preparedApp = originalApp.toBuilder()
                .id(UUID.randomUUID().toString())
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .usuarioId(originalApp.getUsuarioId())
                .monto(originalApp.getMonto())
                .plazo(originalApp.getPlazo())
                .email(originalApp.getEmail())
                .nombreCliente(originalApp.getNombreCliente())
                .prestamoId(originalApp.getPrestamoId())
                .build();

        return Tuples.of(preparedApp, tuple.getT2());
    }

    private Mono<Tuple2<LoanApplication, LoanType>> saveAndKeepContext(Tuple2<LoanApplication, LoanType> tuple) {
        LoanApplication appToSave = tuple.getT1();
        LoanType loanTypeContext = tuple.getT2();

        return loanApplicationRepository.save(appToSave)
                .map(savedApp -> Tuples.of(savedApp, loanTypeContext));
    }

    private Mono<LoanApplication> sendMessageIfRequired(Tuple2<LoanApplication, LoanType> tuple) {
        LoanApplication savedApp = tuple.getT1();
        LoanType loanType = tuple.getT2();

        if (loanType.isAutomaticValidation()) {
            log.info("Tipo de préstamo {} requiere validación automática. Encolando mensaje para solicitud {}",
                    loanType.getId(), savedApp.getId());

            LoanApplication appToUpdate = savedApp.toBuilder()
                    .status(LoanApplicationStatus.PENDIENTE_VALIDACION)
                    .build();

            return loanApplicationRepository.update(appToUpdate)
                    .flatMap(updatedApp -> sqsGateway.sendMessageForValidation(updatedApp)
                            .thenReturn(updatedApp));
        }
        return Mono.just(savedApp);
    }
}
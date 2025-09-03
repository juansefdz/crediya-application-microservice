package co.com.pragma.usecase.loanaplication;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.InvalidLoanApplicationException;
import co.com.pragma.model.customExceptions.LoanTypeNotFoundException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<LoanApplication> createLoanApplication(LoanApplication applicationDraft) {
        log.info("Iniciando CU para crear solicitud. usuarioId={}, prestamoId={}",
                applicationDraft.getUsuarioId(), applicationDraft.getPrestamoId());

        return validateBusinessRules(applicationDraft)
                .map(this::prepareForSave)
                .doOnNext(appToSave -> log.info("OBJETO ANTES DE GUARDAR: {}", appToSave))
                .flatMap(loanApplicationRepository::save)
                .doOnSuccess(saved -> log.info("Solicitud creada OK. id={}, usuarioId={}, estado={}",
                        saved.getId(), saved.getUsuarioId(), saved.getStatus()))
                .doOnError(e -> log.error("Error creando solicitud para usuarioId={}: {}",
                        applicationDraft.getUsuarioId(), e.getMessage()));
    }

    private Mono<LoanApplication> validateBusinessRules(LoanApplication app) {
        return Mono.defer(() -> {
            if (app.getMonto() == null || app.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                return Mono.error(new InvalidLoanApplicationException("El monto de la solicitud debe ser mayor a cero."));
            }
            if (app.getPlazo() == null || app.getPlazo() <= 0) {
                return Mono.error(new InvalidLoanApplicationException("El plazo debe ser de al menos un mes."));
            }
            if (app.getPrestamoId() == null || app.getPrestamoId().isBlank()) {
                return Mono.error(new InvalidLoanApplicationException("El tipo de préstamo es obligatorio."));
            }
            // ⬇️ AQUÍ EL FIX: validar usuarioId, no id
            if (app.getUsuarioId() == null || app.getUsuarioId().isBlank()) {
                return Mono.error(new InvalidLoanApplicationException("La identificación del usuario es obligatoria."));
            }

            try {
                Long loanTypeId = Long.valueOf(app.getPrestamoId());
                return loanTypeRepository.findById(loanTypeId)
                        .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(app.getPrestamoId())))
                        .flatMap(loanType -> {
                            if (app.getMonto().compareTo(loanType.getMontoMinimo()) < 0) {
                                return Mono.error(new InvalidLoanApplicationException(
                                        "El monto solicitado de $" + app.getMonto() + " es menor al mínimo permitido de $" + loanType.getMontoMinimo()));
                            }
                            if (app.getMonto().compareTo(loanType.getMontoMaximo()) > 0) {
                                return Mono.error(new InvalidLoanApplicationException(
                                        "El monto solicitado de $" + app.getMonto() + " es mayor al máximo permitido de $" + loanType.getMontoMaximo()));
                            }
                            return Mono.just(app);
                        });
            } catch (NumberFormatException e) {
                return Mono.error(new InvalidLoanApplicationException(
                        "El ID del tipo de préstamo no es un número válido: " + app.getPrestamoId()));
            }
        });
    }

    private LoanApplication prepareForSave(LoanApplication app) {
        return app.toBuilder()
                .id(UUID.randomUUID().toString())
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .build();
    }
}
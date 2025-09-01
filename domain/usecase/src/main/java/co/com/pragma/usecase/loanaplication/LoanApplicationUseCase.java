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


@Slf4j
@RequiredArgsConstructor
public class LoanApplicationUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<LoanApplication> createLoanApplication(LoanApplication applicationDraft) {
        log.info("Iniciando CU para crear solicitud de préstamo para cliente: {}", applicationDraft.getId());


        return validateBusinessRules(applicationDraft)
                .map(this::setInitialStatus)
                .flatMap(loanApplicationRepository::save)
                .doOnSuccess(savedApp -> log.info("Solicitud creada OK. id={}, cliente={}, estado={}",
                        savedApp.getId(), savedApp.getUsuarioId(), savedApp.getStatus()))
                .doOnError(e -> log.error("Error en CU creando solicitud para cliente {}: {}",
                        applicationDraft.getUsuarioId(), e.getMessage()));
    }


    private Mono<LoanApplication> validateBusinessRules(LoanApplication app) {
        if (app.getMonto() == null || app.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidLoanApplicationException("El monto de la solicitud debe ser mayor a cero."));
        }
        if (app.getPlazo() == null || app.getPlazo() <= 0) {
            return Mono.error(new InvalidLoanApplicationException("El plazo debe ser de al menos un mes."));
        }
        if (app.getPrestamoId() == null || app.getPrestamoId().isBlank()) {
            return Mono.error(new InvalidLoanApplicationException("El tipo de préstamo es obligatorio."));
        }
        if (app.getUsuarioId() == null || app.getUsuarioId().isBlank()) {
            return Mono.error(new InvalidLoanApplicationException("La identificación del cliente es obligatoria."));
        }
        return loanTypeRepository.findById(Long.valueOf(app.getPrestamoId()))
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(app.getPrestamoId())))
                .flatMap(loanType -> {
                    //validación para prestamo
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
    }

    private LoanApplication setInitialStatus(LoanApplication app) {
        return app.toBuilder()
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .build();
    }
}
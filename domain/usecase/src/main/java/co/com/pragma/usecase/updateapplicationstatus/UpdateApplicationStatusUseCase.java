package co.com.pragma.usecase.updateapplicationstatus;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.LoanApplicationNotFoundException;
import co.com.pragma.model.customExceptions.InvalidStatusException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class UpdateApplicationStatusUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final NotificationRepository notificationRepository;

    public Mono<LoanApplication> execute(String id, String nuevoEstadoStr) {
        log.info("CU-INICIO: Solicitud para cambiar estado de id={} a [{}].", id, nuevoEstadoStr);

        final LoanApplicationStatus nuevoEstado;
        try {
            nuevoEstado = LoanApplicationStatus.valueOf(nuevoEstadoStr.toUpperCase());
            if (nuevoEstado != LoanApplicationStatus.APROBADA && nuevoEstado != LoanApplicationStatus.RECHAZADA) {
                return Mono.error(new InvalidStatusException(nuevoEstadoStr));
            }
        } catch (IllegalArgumentException e) {
            return Mono.error(new InvalidStatusException(nuevoEstadoStr));
        }

        return loanApplicationRepository.findById(id)
                .doOnNext(solicitud -> log.info("CU-PASO 1: Solicitud encontrada con estado actual [{}].", solicitud.getStatus()))
                .switchIfEmpty(Mono.error(new LoanApplicationNotFoundException(id)))
                .flatMap(solicitud -> {
                    LoanApplication solicitudActualizada = solicitud.toBuilder()
                            .status(nuevoEstado)
                            .build();
                    log.info("CU-PASO 2: Intentando actualizar solicitud con nuevo estado [{}].", nuevoEstado);
                    return loanApplicationRepository.update(solicitudActualizada)
                            .doOnError(e -> log.error("CU-ERROR: Falla al actualizar en la base de datos.", e));
                })
                .flatMap(solicitudGuardada -> {
                    log.info("CU-PASO 3: Solicitud guardada. Enviando notificación para id={}.", solicitudGuardada.getId());
                    return notificationRepository.sendNotificationCreditReport(solicitudGuardada)
                            .doOnError(e -> log.error("CU-ERROR: Falla al enviar la notificación SQS.", e))
                            .thenReturn(solicitudGuardada);
                })
                .doOnSuccess(res -> log.info("CU-FIN: Proceso completado exitosamente para id={}.", res.getId()))
                .doOnError(e -> log.error("CU-FIN-ERROR: Error final en el flujo para id={}.", id, e));
    }
}
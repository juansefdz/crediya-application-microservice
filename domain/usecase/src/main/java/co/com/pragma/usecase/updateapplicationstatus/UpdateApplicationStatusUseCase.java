package co.com.pragma.usecase.updateapplicationstatus;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UpdateApplicationStatusUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final NotificationRepository notificationRepository;

    public Mono<LoanApplication> execute(String id, String nuevoEstadoStr) {
        log.info("CU-INICIO: Solicitud para cambiar estado de id={} a [{}].", id, nuevoEstadoStr);

        return Mono.fromCallable(() -> LoanApplicationStatus.valueOf(nuevoEstadoStr.toUpperCase()))
                .onErrorMap(IllegalArgumentException.class, e -> new BusinessException(ErrorCode.VAL_STATUS_INVALID, nuevoEstadoStr))
                .filter(estado -> estado == LoanApplicationStatus.APROBADA || estado == LoanApplicationStatus.RECHAZADA)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.VAL_STATUS_INVALID, nuevoEstadoStr)))
                .flatMap(nuevoEstado -> loanApplicationRepository.findById(id)
                        .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.APP_NOT_FOUND, id)))
                        .doOnNext(solicitud -> log.info("CU-PASO 1: Solicitud encontrada con estado actual [{}].", solicitud.getStatus()))
                        .flatMap(solicitud -> {
                            // Valida que la solicitud no esté ya en un estado final
                            List<LoanApplicationStatus> finalStatuses = List.of(LoanApplicationStatus.APROBADA, LoanApplicationStatus.RECHAZADA);
                            if (finalStatuses.contains(solicitud.getStatus())) {
                                // Reporta el estado actual correcto en el error
                                return Mono.error(new BusinessException(ErrorCode.APP_STATE_CONFLICT, solicitud.getStatus().name()));
                            }
                            return Mono.just(solicitud.toBuilder().status(nuevoEstado).build());
                        })
                        .flatMap(solicitudActualizada -> {
                            log.info("CU-PASO 2: Intentando actualizar con nuevo estado [{}].", solicitudActualizada.getStatus());
                            return loanApplicationRepository.update(solicitudActualizada);
                        })
                )
                .flatMap(this::sendNotificationAndContinue)
                .doOnSuccess(res -> log.info("CU-FIN: Proceso completado exitosamente para id={}.", res.getId()))
                .doOnError(BusinessException.class, e -> log.warn("Fallo de negocio controlado [{}]: {}", e.getCode(), e.getMessage()))
                .doOnError(e -> !(e instanceof BusinessException), e -> log.error("CU-FIN-ERROR: Error final en el flujo para id={}.", id, e));
    }

    private Mono<LoanApplication> sendNotificationAndContinue(LoanApplication savedApplication) {
        log.info("CU-PASO 3: Solicitud guardada. Enviando notificación para id={}.", savedApplication.getId());
        return notificationRepository.sendNotificationCreditReport(savedApplication)
                .thenReturn(savedApplication)
                .onErrorResume(error -> {
                    log.error("CU-ERROR: Falla al enviar la notificación, pero la actualización principal fue exitosa.", error);
                    return Mono.just(savedApplication);
                });
    }
}
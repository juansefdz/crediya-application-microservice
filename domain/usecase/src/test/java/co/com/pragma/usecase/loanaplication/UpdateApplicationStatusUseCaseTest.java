package co.com.pragma.usecase.loanaplication;


import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.InvalidStatusException;
import co.com.pragma.model.customExceptions.LoanApplicationNotFoundException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.usecase.updateapplicationstatus.UpdateApplicationStatusUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateApplicationStatusUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

    private LoanApplication initialApplication;
    private final String APP_ID = "app-123";

    @BeforeEach
    void setUp() {
        initialApplication = LoanApplication.builder()
                .id(APP_ID)
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .build();
    }

    @Test
    @DisplayName("Debe actualizar el estado y enviar notificación exitosamente")
    void shouldUpdateStatusAndSendNotificationSuccessfully() {
        LoanApplication updatedApplication = initialApplication.toBuilder()
                .status(LoanApplicationStatus.APROBADA)
                .build();

        when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.just(initialApplication));
        when(loanApplicationRepository.update(any(LoanApplication.class))).thenReturn(Mono.just(updatedApplication));
        when(notificationRepository.sendNotificationCreditReport(any(LoanApplication.class))).thenReturn(Mono.empty().then());

        Mono<LoanApplication> result = updateApplicationStatusUseCase.execute(APP_ID, "APROBADA");

        StepVerifier.create(result)
                .expectNextMatches(app -> app.getStatus() == LoanApplicationStatus.APROBADA)
                .verifyComplete();

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(LoanApplicationStatus.APROBADA);
        assertThat(captor.getValue().getId()).isEqualTo(APP_ID);

        verify(notificationRepository).sendNotificationCreditReport(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Debe retornar LoanApplicationNotFoundException si la solicitud no existe")
    void shouldReturnErrorWhenLoanApplicationNotFound() {
        when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.empty());

        Mono<LoanApplication> result = updateApplicationStatusUseCase.execute(APP_ID, "APROBADA");

        StepVerifier.create(result)
                .expectError(LoanApplicationNotFoundException.class)
                .verify();

        verify(loanApplicationRepository, never()).update(any());
        verify(notificationRepository, never()).sendNotificationCreditReport(any());
    }

    @Test
    @DisplayName("Debe retornar InvalidStatusException para un estado no permitido")
    void shouldReturnErrorForNotAllowedStatus() {
        Mono<LoanApplication> result = updateApplicationStatusUseCase.execute(APP_ID, "PENDIENTE_REVISION");

        StepVerifier.create(result)
                .expectError(InvalidStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar InvalidStatusException para un string de estado inválido")
    void shouldReturnErrorForInvalidStatusString() {
        Mono<LoanApplication> result = updateApplicationStatusUseCase.execute(APP_ID, "ESTADO_INEXISTENTE");

        StepVerifier.create(result)
                .expectError(InvalidStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe completar la actualización aunque la notificación falle (Resiliencia)")
    void shouldSucceedEvenWhenNotificationFails() {
        LoanApplication updatedApplication = initialApplication.toBuilder()
                .status(LoanApplicationStatus.RECHAZADA)
                .build();

        when(notificationRepository.sendNotificationCreditReport(any(LoanApplication.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al conectar con SQS")));

        when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.just(initialApplication));
        when(loanApplicationRepository.update(any(LoanApplication.class))).thenReturn(Mono.just(updatedApplication));

        Mono<LoanApplication> result = updateApplicationStatusUseCase.execute(APP_ID, "RECHAZADA");

        StepVerifier.create(result)
                .expectNextMatches(app -> app.getStatus() == LoanApplicationStatus.RECHAZADA)
                .verifyComplete();

        verify(loanApplicationRepository).update(any(LoanApplication.class));
        verify(notificationRepository).sendNotificationCreditReport(any(LoanApplication.class));
    }
}
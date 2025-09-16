package co.com.pragma.usecase.updateapplicationstatus;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateApplicationStatusUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private UpdateApplicationStatusUseCase useCase;

    private LoanApplication initialApplication;
    private final String APP_ID = "app-123";

    @BeforeEach
    void setUp() {
        initialApplication = LoanApplication.builder()
                .id(APP_ID)
                .status(LoanApplicationStatus.PENDIENTE_REVISION)
                .build();
    }

    @Nested
    @DisplayName("Pruebas de Flujo Exitoso")
    class SuccessFlowTests {
        @Test
        @DisplayName("Debe actualizar estado y enviar notificación exitosamente")
        void shouldUpdateStatusAndSendNotification() {
            // Arrange
            when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.just(initialApplication));
            when(loanApplicationRepository.update(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
            when(notificationRepository.sendNotificationCreditReport(any(LoanApplication.class))).thenReturn(Mono.empty());

            // Act
            Mono<LoanApplication> result = useCase.execute(APP_ID, "APROBADA");

            // Assert
            StepVerifier.create(result)
                    .expectNextMatches(app -> app.getStatus() == LoanApplicationStatus.APROBADA)
                    .verifyComplete();

            verify(loanApplicationRepository).update(any(LoanApplication.class));
            verify(notificationRepository).sendNotificationCreditReport(any(LoanApplication.class));
        }

        @Test
        @DisplayName("Debe completar la actualización aunque la notificación falle")
        void shouldSucceedEvenWhenNotificationFails() {
            // Arrange
            when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.just(initialApplication));
            when(loanApplicationRepository.update(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
            when(notificationRepository.sendNotificationCreditReport(any(LoanApplication.class)))
                    .thenReturn(Mono.error(new RuntimeException("SQS service is down")));

            // Act
            Mono<LoanApplication> result = useCase.execute(APP_ID, "RECHAZADA");

            // Assert
            StepVerifier.create(result)
                    .expectNextMatches(app -> app.getStatus() == LoanApplicationStatus.RECHAZADA)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Pruebas de Flujo de Error")
    class ErrorFlowTests {
        private void assertBusinessException(Mono<?> mono, ErrorCode expectedCode) {
            StepVerifier.create(mono)
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                                    ((BusinessException) throwable).getErrorCode() == expectedCode)
                    .verify();
        }

        @Test
        @DisplayName("Debe retornar error si la solicitud no se encuentra")
        void shouldReturnErrorWhenApplicationNotFound() {
            when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.empty());
            assertBusinessException(useCase.execute(APP_ID, "APROBADA"), ErrorCode.APP_NOT_FOUND);
        }

        @Test
        @DisplayName("Debe retornar error si el nuevo estado es inválido")
        void shouldReturnErrorForInvalidStatusString() {
            assertBusinessException(useCase.execute(APP_ID, "ESTADO_INVALIDO"), ErrorCode.VAL_STATUS_INVALID);
        }

        @Test
        @DisplayName("Debe retornar error si se intenta actualizar una solicitud en estado final")
        void shouldReturnErrorWhenApplicationIsInAFinalState() {
            // Arrange
            LoanApplication alreadyApprovedApp = initialApplication.toBuilder()
                    .status(LoanApplicationStatus.APROBADA)
                    .build();
            when(loanApplicationRepository.findById(APP_ID)).thenReturn(Mono.just(alreadyApprovedApp));

            // Act & Assert
            assertBusinessException(useCase.execute(APP_ID, "RECHAZADA"), ErrorCode.APP_STATE_CONFLICT);

            // Verificamos que no se intentó hacer el update
            verify(loanApplicationRepository, never()).update(any());
        }
    }
}
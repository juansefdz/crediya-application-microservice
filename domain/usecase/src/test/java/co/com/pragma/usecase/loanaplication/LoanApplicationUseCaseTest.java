package co.com.pragma.usecase.loanaplication;

import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.sqs.gateways.SqsNotificationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock private LoanApplicationRepository loanApplicationRepository;
    @Mock private LoanTypeRepository loanTypeRepository;
    @Mock private SqsNotificationGateway sqsGateway;
    @InjectMocks private LoanApplicationUseCase loanApplicationUseCase;

    private LoanApplication applicationDraft;
    private LoanType loanTypeWithAutoValidation;
    private LoanType loanTypeWithoutAutoValidation;

    @BeforeEach
    void setUp() {
        applicationDraft = LoanApplication.builder()
                .usuarioId("user-123")
                .prestamoId(1L)
                .monto(new BigDecimal("20000"))
                .plazo(24)
                .email("test@test.com")
                .nombreCliente("Juan Perez")
                .build();

        loanTypeWithAutoValidation = LoanType.builder()
                .id(String.valueOf(1L)).montoMinimo(BigDecimal.ONE).montoMaximo(new BigDecimal("100000"))
                .automaticValidation(true).build();

        loanTypeWithoutAutoValidation = LoanType.builder()
                .id(String.valueOf(2L)).montoMinimo(BigDecimal.ONE).montoMaximo(new BigDecimal("100000"))
                .automaticValidation(false).build();
    }

    @Test
    @DisplayName("Debe guardar y encolar a SQS si la validación automática está activa")
    void shouldSaveAndEnqueueWhenAutoValidationIsTrue() {
        // Arrange
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(loanTypeWithAutoValidation));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(loanApplicationRepository.update(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(sqsGateway.sendMessageForValidation(any(LoanApplication.class))).thenReturn(Mono.empty());

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.createLoanApplication(applicationDraft);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getStatus() == LoanApplicationStatus.PENDIENTE_VALIDACION)
                .verifyComplete();

        verify(loanApplicationRepository).save(any(LoanApplication.class));
        verify(loanApplicationRepository).update(any(LoanApplication.class));
        verify(sqsGateway).sendMessageForValidation(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Debe solo guardar si la validación automática está inactiva")
    void shouldOnlySaveWhenAutoValidationIsFalse() {
        // Arrange
        LoanApplication draft = applicationDraft.toBuilder().prestamoId(2L).build();
        when(loanTypeRepository.findById(2L)).thenReturn(Mono.just(loanTypeWithoutAutoValidation));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.createLoanApplication(draft);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getStatus() == LoanApplicationStatus.PENDIENTE_REVISION)
                .verifyComplete();

        verify(loanApplicationRepository).save(any(LoanApplication.class));
        verify(loanApplicationRepository, never()).update(any(LoanApplication.class));
        verify(sqsGateway, never()).sendMessageForValidation(any(LoanApplication.class));
    }

    @Test
    @DisplayName("Debe retornar error si el tipo de préstamo no existe")
    void shouldReturnErrorWhenLoanTypeNotFound() {
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(loanApplicationUseCase.createLoanApplication(applicationDraft))
                .expectErrorMatches(ex -> ex instanceof BusinessException && ((BusinessException) ex).getErrorCode() == ErrorCode.LTY_NOT_FOUND)
                .verify();
    }
}
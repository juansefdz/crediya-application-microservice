package co.com.pragma.usecase.loanaplication;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.InvalidLoanApplicationException;
import co.com.pragma.model.customExceptions.LoanTypeNotFoundException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanTypeRepository loanTypeRepository;

    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase;

    @Captor
    private ArgumentCaptor<LoanApplication> applicationCaptor;

    private LoanApplication validApplicationDraft;
    private LoanType validLoanType;
    private final String LOAN_TYPE_ID = "d290f1ee-6c54-4b01-90e6-d701748f0851";


    @BeforeEach
    void setUp() {
        validApplicationDraft = LoanApplication.builder()
                .usuarioId("user-123")
                .prestamoId("1")
                .monto(new BigDecimal("10000"))
                .plazo(12)
                .build();

        validLoanType = LoanType.builder()
                .id(LOAN_TYPE_ID)
                .nombre("Crédito de Consumo")
                .montoMinimo(new BigDecimal("1000"))
                .montoMaximo(new BigDecimal("50000"))
                .build();
    }

    @Test
    @DisplayName("Debe crear una solicitud exitosamente cuando todos los datos son válidos")
    void shouldCreateLoanApplicationSuccessfully() {
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(validLoanType));
        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<LoanApplication> result = loanApplicationUseCase.createLoanApplication(validApplicationDraft);

        StepVerifier.create(result)
                .expectNextMatches(savedApp -> {
                    assertThat(savedApp.getStatus()).isEqualTo(LoanApplicationStatus.PENDIENTE_REVISION);
                    assertThat(savedApp.getId()).isNotNull();
                    assertThat(savedApp.getUsuarioId()).isEqualTo("user-123");
                    return true;
                })
                .verifyComplete();

        verify(loanApplicationRepository).save(applicationCaptor.capture());
        LoanApplication capturedApp = applicationCaptor.getValue();
        assertThat(capturedApp.getStatus()).isEqualTo(LoanApplicationStatus.PENDIENTE_REVISION);
        assertThat(capturedApp.getMonto()).isEqualTo(new BigDecimal("10000"));
    }

    @Nested
    @DisplayName("Pruebas de validación de reglas de negocio")
    class BusinessRuleValidationTests {

        @Test
        @DisplayName("Debe fallar si el monto es nulo o cero")
        void shouldFailWhenAmountIsInvalid() {
            LoanApplication invalidApp = validApplicationDraft.toBuilder().monto(null).build();
            StepVerifier.create(loanApplicationUseCase.createLoanApplication(invalidApp))
                    .expectError(InvalidLoanApplicationException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el plazo es nulo o cero")
        void shouldFailWhenTermIsInvalid() {
            LoanApplication invalidApp = validApplicationDraft.toBuilder().plazo(0).build();
            StepVerifier.create(loanApplicationUseCase.createLoanApplication(invalidApp))
                    .expectError(InvalidLoanApplicationException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el ID de usuario está en blanco")
        void shouldFailWhenUserIdIsMissing() {
            LoanApplication invalidApp = validApplicationDraft.toBuilder().usuarioId("").build();
            StepVerifier.create(loanApplicationUseCase.createLoanApplication(invalidApp))
                    .expectError(InvalidLoanApplicationException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el tipo de préstamo no se encuentra")
        void shouldFailWhenLoanTypeIsNotFound() {
            when(loanTypeRepository.findById(anyLong())).thenReturn(Mono.empty());

            StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                    .expectError(LoanTypeNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el ID del tipo de préstamo no es un número")
        void shouldFailWhenLoanTypeIdIsNotANumber() {
            LoanApplication invalidApp = validApplicationDraft.toBuilder().prestamoId("invalid-id").build();
            StepVerifier.create(loanApplicationUseCase.createLoanApplication(invalidApp))
                    .expectError(InvalidLoanApplicationException.class)
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el monto es menor al mínimo permitido")
        void shouldFailWhenAmountIsBelowMinimum() {
            when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(validLoanType));
            LoanApplication invalidApp = validApplicationDraft.toBuilder().monto(new BigDecimal("500")).build();

            StepVerifier.create(loanApplicationUseCase.createLoanApplication(invalidApp))
                    .expectErrorMatches(e -> e instanceof InvalidLoanApplicationException && e.getMessage().contains("menor al mínimo permitido"))
                    .verify();
        }

        @Test
        @DisplayName("Debe fallar si el monto es mayor al máximo permitido")
        void shouldFailWhenAmountIsAboveMaximum() {
            when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(validLoanType));
            LoanApplication invalidApp = validApplicationDraft.toBuilder().monto(new BigDecimal("60000")).build();

            StepVerifier.create(loanApplicationUseCase.createLoanApplication(invalidApp))
                    .expectErrorMatches(e -> e instanceof InvalidLoanApplicationException && e.getMessage().contains("mayor al máximo permitido"))
                    .verify();
        }
    }
}
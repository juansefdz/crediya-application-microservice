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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository; // Dependencia Mock

    @Mock
    private LoanTypeRepository loanTypeRepository; // Dependencia Mock

    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase; // La clase que probamos

    private LoanApplication validApplicationDraft;
    private LoanType validLoanType;

    @BeforeEach
    void setUp() {
        // Objeto base para una solicitud válida que usaremos en múltiples pruebas
        validApplicationDraft = LoanApplication.builder()
                .usuarioId("user-123")
                .prestamoId("1")
                .monto(new BigDecimal("10000"))
                .plazo(12)
                .build();

        // Objeto base para un tipo de préstamo válido
        validLoanType = LoanType.builder()
                .id(1L)
                .nombre("Crédito de Libre Inversión")
                .montoMinimo(new BigDecimal("1000"))
                .montoMaximo(new BigDecimal("50000"))
                .build();
    }

    @Test
    @DisplayName("Debe crear una solicitud de préstamo exitosamente cuando todos los datos son válidos")
    void shouldCreateLoanApplicationSuccessfully() {
        // Arrange: Preparación del escenario
        // 1. Simulamos que el tipo de préstamo existe
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(validLoanType));

        // 2. Simulamos la operación de guardado en el repositorio
        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> {
                    LoanApplication app = invocation.getArgument(0);
                    // Devolvemos el objeto como lo haría el repositorio real (con ID y estado)
                    return Mono.just(app);
                });

        // Act: Ejecutamos el caso de uso
        Mono<LoanApplication> result = loanApplicationUseCase.createLoanApplication(validApplicationDraft);

        // Assert: Verificamos el resultado
        StepVerifier.create(result)
                .assertNext(savedApp -> {
                    assertThat(savedApp.getId()).isNotNull();
                    assertThat(savedApp.getStatus()).isEqualTo(LoanApplicationStatus.PENDIENTE_REVISION);
                    assertThat(savedApp.getUsuarioId()).isEqualTo("user-123");
                })
                .verifyComplete();

        // Verificamos que los métodos de los mocks fueron llamados como se esperaba
        verify(loanTypeRepository).findById(1L);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    // --- Pruebas para Validaciones de Entrada ---

    @Test
    @DisplayName("Debe retornar error si el monto es nulo")
    void shouldReturnErrorWhenAmountIsNull() {
        validApplicationDraft.setMonto(null);

        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectError(InvalidLoanApplicationException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar error si el plazo es cero")
    void shouldReturnErrorWhenTermIsZero() {
        validApplicationDraft.setPlazo(0);

        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectError(InvalidLoanApplicationException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar error si el usuarioId es nulo o vacío")
    void shouldReturnErrorWhenUserIdIsBlank() {
        validApplicationDraft.setUsuarioId("");

        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectError(InvalidLoanApplicationException.class)
                .verify();
    }

    // --- Pruebas para Reglas de Negocio con Dependencias ---

    @Test
    @DisplayName("Debe retornar LoanTypeNotFoundException si el tipo de préstamo no existe")
    void shouldReturnErrorWhenLoanTypeNotFound() {
        // Arrange: Simulamos que el repositorio no encuentra el tipo de préstamo
        when(loanTypeRepository.findById(anyLong())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectError(LoanTypeNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Debe retornar error si el monto es menor al mínimo permitido")
    void shouldReturnErrorWhenAmountIsBelowMinimum() {
        // Arrange
        validApplicationDraft.setMonto(new BigDecimal("500")); // Monto por debajo del mínimo de 1000
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(validLoanType));

        // Act & Assert
        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidLoanApplicationException &&
                                throwable.getMessage().contains("menor al mínimo permitido"))
                .verify();
    }

    @Test
    @DisplayName("Debe retornar error si el monto es mayor al máximo permitido")
    void shouldReturnErrorWhenAmountIsAboveMaximum() {
        // Arrange
        validApplicationDraft.setMonto(new BigDecimal("60000")); // Monto por encima del máximo de 50000
        when(loanTypeRepository.findById(1L)).thenReturn(Mono.just(validLoanType));

        // Act & Assert
        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidLoanApplicationException &&
                                throwable.getMessage().contains("mayor al máximo permitido"))
                .verify();
    }

    @Test
    @DisplayName("Debe retornar error si el ID del tipo de préstamo no es un número válido")
    void shouldReturnErrorForInvalidLoanTypeIdFormat() {
        // Arrange
        validApplicationDraft.setPrestamoId("TIPO_INVALIDO");

        // Act & Assert
        StepVerifier.create(loanApplicationUseCase.createLoanApplication(validApplicationDraft))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidLoanApplicationException &&
                                throwable.getMessage().contains("no es un número válido"))
                .verify();
    }
}
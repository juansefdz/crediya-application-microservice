package co.com.pragma.usecase.listapplications;

import co.com.pragma.model.DataPage;
import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListApplicationsUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @InjectMocks
    private ListApplicationsUseCase listApplicationsUseCase;
    @Captor
    private ArgumentCaptor<List<Integer>> statusIdsCaptor;
    private List<LoanApplication> sampleApplications;

    @BeforeEach
    void setUp() {
        sampleApplications = List.of(
                LoanApplication.builder().id("app-1").build(),
                LoanApplication.builder().id("app-2").build()
        );
    }

    @Nested
    @DisplayName("Pruebas para el método findAll")
    class FindAllTests {
        @Test
        @DisplayName("Debe retornar una respuesta paginada de todas las solicitudes exitosamente")
        void shouldReturnPaginatedResponseOfAllApplications() {
            when(loanApplicationRepository.countAll()).thenReturn(Mono.just(5L));
            when(loanApplicationRepository.findAll(0, 2, "id", "ASC"))
                    .thenReturn(Flux.fromIterable(sampleApplications));

            Mono<DataPage<LoanApplication>> result = listApplicationsUseCase.findAll(0, 2, "id", "ASC");

            StepVerifier.create(result)
                    .expectNextMatches(page -> {
                        assertThat(page.getContent()).hasSize(2);
                        assertThat(page.getCurrentPage()).isEqualTo(0);
                        assertThat(page.getTotalElements()).isEqualTo(5L);
                        assertThat(page.getTotalPages()).isEqualTo(3);
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe retornar BusinessException si la paginación es inválida")
        void shouldReturnErrorForInvalidPagination() {
            StepVerifier.create(listApplicationsUseCase.findAll(-1, 10, "id", "ASC"))
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                                    ((BusinessException) throwable).getErrorCode() == ErrorCode.VAL_PAGINATION_INVALID)
                    .verify();

            verifyNoInteractions(loanApplicationRepository);
        }
    }

    @Nested
    @DisplayName("Pruebas para el método findForReview")
    class FindForReviewTests {
        @Test
        @DisplayName("Debe retornar una respuesta paginada de solicitudes para revisión exitosamente")
        void shouldReturnPaginatedResponseOfApplicationsForReview() {
            when(loanApplicationRepository.countByStatusIn(anyList())).thenReturn(Mono.just(10L));
            when(loanApplicationRepository.findByStatusIn(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(Flux.fromIterable(sampleApplications));

            Mono<DataPage<LoanApplication>> result = listApplicationsUseCase.findForReview(1, 5, "monto", "DESC");

            StepVerifier.create(result)
                    .expectNextMatches(page -> {
                        assertThat(page.getContent()).hasSize(2);
                        assertThat(page.getCurrentPage()).isEqualTo(1);
                        assertThat(page.getTotalElements()).isEqualTo(10L);
                        assertThat(page.getTotalPages()).isEqualTo(2);
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Debe retornar BusinessException si la paginación es inválida")
        void shouldReturnErrorForInvalidPagination() {
            StepVerifier.create(listApplicationsUseCase.findForReview(0, 0, "id", "ASC"))
                    .expectErrorMatches(throwable ->
                            throwable instanceof BusinessException &&
                                    ((BusinessException) throwable).getErrorCode() == ErrorCode.VAL_PAGINATION_INVALID)
                    .verify();
        }
    }
}
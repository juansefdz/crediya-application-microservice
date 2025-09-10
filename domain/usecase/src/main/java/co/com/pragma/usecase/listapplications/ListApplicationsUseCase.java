package co.com.pragma.usecase.listapplications;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.PageResponse;
import co.com.pragma.model.customExceptions.InvalidPaginationException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ListApplicationsUseCase {

    private final LoanApplicationRepository loanApplicationRepository;

    private static final List<LoanApplicationStatus> ESTADOS_REVISION = List.of(
            LoanApplicationStatus.PENDIENTE_REVISION,
            LoanApplicationStatus.RECHAZADA,
            LoanApplicationStatus.REVISION_MANUAL
    );

    public Mono<PageResponse<LoanApplication>> execute(int page, int size, String sortBy, String sortOrder) {
        log.info("CU: Obteniendo solicitudes para revisión. Página: {}, Tamaño: {}", page, size);

        if (page < 0 || size <= 0) {
            return Mono.error(new InvalidPaginationException("Los parámetros de página y tamaño deben ser positivos."));
        }

        List<Integer> statusIds = ESTADOS_REVISION.stream()
                .map(LoanApplicationStatus::getId)
                .toList();

        Mono<Long> totalElementsMono = loanApplicationRepository.countByStatusIn(statusIds);
        Mono<List<LoanApplication>> contentMono = loanApplicationRepository
                .findByStatusIn(statusIds, page, size, sortBy, sortOrder)
                .collectList();

        return Mono.zip(contentMono, totalElementsMono)
                .map(tuple -> {
                    List<LoanApplication> applications = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    int totalPages = (size > 0) ? (int) Math.ceil((double) totalElements / size) : 0;

                    return PageResponse.<LoanApplication>builder()
                            .content(applications)
                            .currentPage(page)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .build();
                });
    }
}
package co.com.pragma.usecase.listapplications;

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
public class ListAllApplicationsUseCase {

    private final LoanApplicationRepository loanApplicationRepository;

    public Mono<PageResponse<LoanApplication>> execute(int page, int size, String sortBy, String sortOrder) {
        log.info("CU: Obteniendo todas las solicitudes. Página: {}, Tamaño: {}", page, size);

        if (page < 0 || size <= 0) {
            return Mono.error(new InvalidPaginationException("Los parámetros de página y tamaño deben ser positivos."));
        }

        Mono<Long> totalElementsMono = loanApplicationRepository.countAll();


        Mono<List<LoanApplication>> contentMono = loanApplicationRepository
                .findAll(page, size, sortBy, sortOrder)
                .collectList();

        return Mono.zip(contentMono, totalElementsMono)
                .map(tuple -> {
                    List<LoanApplication> applications = tuple.getT1();
                    Long totalElements = tuple.getT2();

                    int totalPages = (int) Math.ceil((double) totalElements / size);

                    return PageResponse.<LoanApplication>builder()
                            .content(applications)
                            .currentPage(page)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .build();
                });
    }
}
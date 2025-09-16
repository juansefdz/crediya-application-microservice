package co.com.pragma.usecase.listapplications;

import co.com.pragma.model.DataPage;
import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ListApplicationsUseCase {

    private final LoanApplicationRepository loanApplicationRepository;

    private static final List<LoanApplicationStatus> ESTADOS_PARA_REVISION = List.of(
            LoanApplicationStatus.PENDIENTE_REVISION,
            LoanApplicationStatus.RECHAZADA,
            LoanApplicationStatus.REVISION_MANUAL
    );

    public Mono<DataPage<LoanApplication>> findAll(int page, int size, String sortBy, String sortOrder) {
        log.info("CU: Obteniendo todas las solicitudes. Página: {}, Tamaño: {}", page, size);

        return validatePagination(page, size).then(Mono.defer(() -> {
            Mono<Long> totalElementsMono = loanApplicationRepository.countAll();
            Mono<List<LoanApplication>> contentMono = loanApplicationRepository
                    .findAll(page, size, sortBy, sortOrder)
                    .collectList();
            return buildPageResponse(contentMono, totalElementsMono, page, size);
        }));
    }

    public Mono<DataPage<LoanApplication>> findForReview(int page, int size, String sortBy, String sortOrder) {
        log.info("CU: Obteniendo solicitudes para revisión. Página: {}, Tamaño: {}", page, size);

        return validatePagination(page, size).then(Mono.defer(() -> {
            List<String> statusNames = ESTADOS_PARA_REVISION.stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());

            Mono<Long> totalElementsMono = loanApplicationRepository.countByStatusIn(statusNames);
            Mono<List<LoanApplication>> contentMono = loanApplicationRepository
                    .findByStatusIn(statusNames, page, size, sortBy, sortOrder)
                    .collectList();
            return buildPageResponse(contentMono, totalElementsMono, page, size);
        }));
    }

    private Mono<Void> validatePagination(int page, int size) {
        if (page < 0 || size <= 0) {
            return Mono.error(new BusinessException(ErrorCode.VAL_PAGINATION_INVALID));
        }
        return Mono.empty();
    }

    private Mono<DataPage<LoanApplication>> buildPageResponse(Mono<List<LoanApplication>> contentMono, Mono<Long> totalElementsMono, int page, int size) {
        return Mono.zip(contentMono, totalElementsMono)
                .map(tuple -> {
                    List<LoanApplication> applications = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    int totalPages = (size > 0) ? (int) Math.ceil((double) totalElements / size) : 0;

                    return DataPage.<LoanApplication>builder()
                            .content(applications)
                            .currentPage(page)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .build();
                });
    }
}
package co.com.pragma.api;


import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationSummaryDTO;
import co.com.pragma.api.dto.PageResponseDTO;
import co.com.pragma.api.mapper.LoanApplicationApiMapper;
import co.com.pragma.usecase.listapplications.ListApplicationsUseCase;
import co.com.pragma.usecase.loanaplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Handler {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationApiMapper apiMapper;
    private final ListApplicationsUseCase listApplicationsUseCase;

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        return request.bodyToMono(LoanApplicationRequestDTO.class)
                .map(apiMapper::toDomain)
                .flatMap(loanApplicationUseCase::createLoanApplication)
                .map(apiMapper::toDTO)
                .flatMap(dto -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(dto)
                );
    }

    public Mono<ServerResponse> obtenerSolicitudes(ServerRequest request) {

        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        String sortBy = request.queryParam("sortBy").orElse("monto");
        String sortOrder = request.queryParam("sortOrder").orElse("ASC");

        return listApplicationsUseCase.execute(page, size, sortBy, sortOrder)
                .flatMap(pageResponse -> {

                    List<LoanApplicationSummaryDTO> dtoList = pageResponse.getContent().stream()
                            .map(apiMapper::toSummaryDTO)
                            .toList();


                    PageResponseDTO<LoanApplicationSummaryDTO> responseDto = PageResponseDTO.<LoanApplicationSummaryDTO>builder()
                            .content(dtoList)
                            .currentPage(pageResponse.getCurrentPage())
                            .totalElements(pageResponse.getTotalElements())
                            .totalPages(pageResponse.getTotalPages())
                            .build();

                    return ServerResponse.ok().bodyValue(responseDto);
                });
    }
}

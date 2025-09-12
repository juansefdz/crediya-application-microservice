package co.com.pragma.api;

import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationSummaryDTO;
import co.com.pragma.api.dto.PageResponseDTO;
import co.com.pragma.api.dto.UpdateStatusRequestDTO;
import co.com.pragma.api.mapper.LoanApplicationApiMapper;
import co.com.pragma.model.DataPage;
import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.usecase.listapplications.ListApplicationsUseCase;
import co.com.pragma.usecase.loanaplication.LoanApplicationUseCase;
import co.com.pragma.usecase.updateapplicationstatus.UpdateApplicationStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Handler {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final ListApplicationsUseCase listApplicationsUseCase;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;
    private final LoanApplicationApiMapper apiMapper;

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        return request.bodyToMono(LoanApplicationRequestDTO.class)
                .map(apiMapper::toDomain)
                .flatMap(loanApplicationUseCase::createLoanApplication)
                .map(apiMapper::toDTO)
                .flatMap(dto -> ServerResponse.status(HttpStatus.CREATED).bodyValue(dto))
                .onErrorResume(BusinessException.class, e -> ServerResponse.badRequest().bodyValue(e.getMessage()))
                .onErrorResume(Exception.class, e -> {
                    log.error("HANDLER: Error inesperado al crear solicitud:", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue("Error inesperado.");
                });
    }

    /**
     * Endpoint para obtener las solicitudes que necesitan revisión.
     */
    public Mono<ServerResponse> obtenerSolicitudesParaRevision(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        String sortBy = request.queryParam("sortBy").orElse("monto");
        String sortOrder = request.queryParam("sortOrder").orElse("ASC");

        Mono<DataPage<LoanApplication>> pageData = listApplicationsUseCase.findForReview(page, size, sortBy, sortOrder);
        return buildPaginatedResponse(pageData);
    }

    /**
     * Endpoint para obtener TODAS las solicitudes.
     */
    public Mono<ServerResponse> listAllApplications(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        String sortBy = request.queryParam("sortBy").orElse("created_at");
        String sortOrder = request.queryParam("sortOrder").orElse("DESC");

        Mono<DataPage<LoanApplication>> pageData = listApplicationsUseCase.findAll(page, size, sortBy, sortOrder);
        return buildPaginatedResponse(pageData);
    }

    public Mono<ServerResponse> updateApplicationStatus(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateStatusRequestDTO.class)
                .flatMap(dto -> updateApplicationStatusUseCase.execute(id, dto.getNuevoEstado()))
                .map(apiMapper::toDTO)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .onErrorResume(BusinessException.class, e -> ServerResponse.badRequest().bodyValue(e.getMessage()))
                .onErrorResume(Exception.class, e -> {
                    log.error("HANDLER: Error inesperado al actualizar {}:", id, e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue("Error inesperado.");
                });
    }

    private Mono<ServerResponse> buildPaginatedResponse(Mono<DataPage<LoanApplication>> pageResponseMono) {
        return pageResponseMono.flatMap(pageResponse -> {
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
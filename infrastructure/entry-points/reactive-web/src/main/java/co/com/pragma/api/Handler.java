package co.com.pragma.api;


import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationSummaryDTO;
import co.com.pragma.api.dto.PageResponseDTO;
import co.com.pragma.api.dto.UpdateStatusRequestDTO;
import co.com.pragma.api.mapper.LoanApplicationApiMapper;
import co.com.pragma.model.customExceptions.BusinessException;
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
    private final LoanApplicationApiMapper apiMapper;
    private final ListApplicationsUseCase listApplicationsUseCase;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

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

    public Mono<ServerResponse> updateApplicationStatus(ServerRequest request) {
        String id = request.pathVariable("id");
        log.info("HANDLER: Recibida petición PUT para id={}", id);

        return request.bodyToMono(UpdateStatusRequestDTO.class)
                .doOnError(e -> log.error("HANDLER-ERROR: Falla al convertir el JSON del body:", e))
                .flatMap(dto -> {
                    log.info("HANDLER: Body convertido a DTO exitosamente: {}", dto);
                    return updateApplicationStatusUseCase.execute(id, dto.getNuevoEstado());
                })
                .map(apiMapper::toDTO)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .onErrorResume(BusinessException.class, e -> {
                    log.error("HANDLER: Error de negocio controlado:", e);
                    return ServerResponse.badRequest().bodyValue(e.getMessage());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("HANDLER: Error inesperado no controlado:", e);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue("Error inesperado.");
                });
    }
}

package co.com.pragma.api;


import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.mapper.LoanApplicationApiMapper;
import co.com.pragma.usecase.loanaplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
@Component
@RequiredArgsConstructor
public class Handler {

    private final LoanApplicationUseCase loanApplicationUseCase;
    private final LoanApplicationApiMapper apiMapper;

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
}

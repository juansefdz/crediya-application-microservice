package co.com.pragma.api;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.usecase.loanaplication.LoanApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final LoanApplicationUseCase loanApplicationUseCase;

    public Mono<ServerResponse> createLoanApplication(ServerRequest request) {
        return request.bodyToMono(LoanApplication.class)
                .flatMap(loanApplicationUseCase::createLoanApplication)
                .flatMap(saved -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(saved)
                )
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }
}

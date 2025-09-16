package co.com.pragma.lambda.debtcapacoty;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.loanapplication.LoanApplication;

import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class HttpHttpLoanApplicationAdapter implements LoanApplicationRepository {

    private final WebClient webClient;

    @Override
    public Mono<LoanApplication> findById(String id) {
        return webClient.get().uri("/api/v1/internal/solicitudes/{id}", id).retrieve().bodyToMono(LoanApplication.class);
    }

    @Override
    public Flux<LoanApplication> findByUserIdAndStatus(String userId, LoanApplicationStatus status) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/internal/solicitudes")
                        .queryParam("userId", userId)
                        .queryParam("status", status.name())
                        .build())
                .retrieve()
                .bodyToFlux(LoanApplication.class);
    }

    // --- MÉTODOS RESTANTES IMPLEMENTADOS PARA CUMPLIR LA INTERFAZ ---
    @Override
    public Mono<LoanApplication> save(LoanApplication app) {
        return Mono.error(new UnsupportedOperationException("Operación 'save' no soportada por este adaptador."));
    }
    @Override
    public Mono<LoanApplication> update(LoanApplication app) {
        return Mono.error(new UnsupportedOperationException("Operación 'update' no soportada por este adaptador."));
    }
    @Override
    public Flux<LoanApplication> findByStatusIn(List<String> statusNames, int page, int size, String sortBy, String sortOrder) {
        return Flux.error(new UnsupportedOperationException("Operáción 'findByStatusIn' no soportada por este adaptador."));
    }
    @Override
    public Mono<Long> countByStatusIn(List<String> statusNames) {
        return Mono.error(new UnsupportedOperationException("Operación 'countByStatusIn' no soportada por este adaptador."));
    }
    @Override
    public Mono<Long> countAll() {
        return Mono.error(new UnsupportedOperationException("Operación 'countAll' no soportada por este adaptador."));
    }
    @Override
    public Flux<LoanApplication> findAll(int page, int size, String sortBy, String sortOrder) {
        return Flux.error(new UnsupportedOperationException("Operación 'findAll' no soportada por este adaptador."));
    }
}
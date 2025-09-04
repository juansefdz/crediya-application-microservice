package co.com.pragma.model.loanapplication.gateways;


import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface LoanApplicationRepository {
    Mono<LoanApplication> save(LoanApplication solicitud);

    Flux<LoanApplication> findByStatusIn(List<Integer> statusIds, int page, int size, String sortBy, String sortOrder);

    Mono<Long> countByStatusIn(List<Integer> statusIds);
}


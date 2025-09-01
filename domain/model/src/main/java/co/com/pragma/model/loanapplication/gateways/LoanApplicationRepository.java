package co.com.pragma.model.loanapplication.gateways;


import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.print.Pageable;

public interface LoanApplicationRepository {
    Mono<LoanApplication> save(LoanApplication solicitud);

    Flux<LoanApplication> findApplications(Pageable pageable);
}


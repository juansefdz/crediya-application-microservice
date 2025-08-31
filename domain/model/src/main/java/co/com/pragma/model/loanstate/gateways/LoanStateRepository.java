package co.com.pragma.model.loanstate.gateways;

import co.com.pragma.model.loanstate.LoanState;
import reactor.core.publisher.Mono;

public interface LoanStateRepository {
    Mono<LoanState> findByNombre(String nombre);
}

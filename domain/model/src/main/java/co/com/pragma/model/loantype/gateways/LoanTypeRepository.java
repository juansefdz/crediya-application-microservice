package co.com.pragma.model.loantype.gateways;

import co.com.pragma.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<Boolean> existsById(Long id);
    Mono<LoanType> findById(Long id);
    Mono<LoanType> save(LoanType loanType);
}

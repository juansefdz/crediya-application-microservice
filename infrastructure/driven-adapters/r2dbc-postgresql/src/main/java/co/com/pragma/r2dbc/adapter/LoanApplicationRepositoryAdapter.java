package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.r2dbc.mapper.LoanApplicationPersistenceMapper;
import co.com.pragma.r2dbc.repository.LoanApplicationDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class LoanApplicationRepositoryAdapter implements LoanApplicationRepository {
    private final LoanApplicationDataRepository dataRepository;
    private final LoanApplicationPersistenceMapper mapper;

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        return dataRepository.save(mapper.toData(loanApplication))
                .map(mapper::toDomain);
    }
}
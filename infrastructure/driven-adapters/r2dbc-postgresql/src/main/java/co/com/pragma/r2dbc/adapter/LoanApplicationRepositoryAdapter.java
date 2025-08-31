package co.com.pragma.r2dbc.adapter;


import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;

import co.com.pragma.r2dbc.mapper.LoanApplicationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
@RequiredArgsConstructor
@Repository
public class LoanApplicationRepositoryAdapter implements LoanApplicationRepository {
    private final co.com.pragma.r2dbc.repository.LoanApplicationDataRepository repository;
    private final LoanApplicationPersistenceMapper mapper;

    @Override
    public Mono<LoanApplication> save(LoanApplication solicitud) {
        return repository.save(mapper.toData(solicitud))
                .map(mapper::toDomain);
    }
}
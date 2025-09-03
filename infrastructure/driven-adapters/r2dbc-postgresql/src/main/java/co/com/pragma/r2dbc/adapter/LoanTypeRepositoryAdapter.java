package co.com.pragma.r2dbc.adapter;


import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.r2dbc.mapper.LoanTypePersistenceMapper;
import co.com.pragma.r2dbc.repository.LoanTypeDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {

    private final LoanTypeDataRepository repository;
    private final LoanTypePersistenceMapper mapper;

    @Override
    public Mono<Boolean> existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<LoanType> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<LoanType> save(LoanType loanType) {
        return repository.save(mapper.toData(loanType))
                .map(mapper::toDomain);
    }
}
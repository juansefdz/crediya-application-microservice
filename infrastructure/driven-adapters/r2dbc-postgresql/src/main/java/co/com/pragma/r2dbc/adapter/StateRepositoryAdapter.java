package co.com.pragma.r2dbc.adapter;


import co.com.pragma.model.loanstate.LoanState;
import co.com.pragma.model.loanstate.gateways.LoanStateRepository;
import co.com.pragma.r2dbc.mapper.StatePersistenceMapper;
import co.com.pragma.r2dbc.repository.StateDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
@RequiredArgsConstructor
@Repository
public class StateRepositoryAdapter implements LoanStateRepository {
    private final StateDataRepository repository;
    private final StatePersistenceMapper mapper;

    @Override
    public Mono<LoanState> findByNombre(String nombre) {
        return repository.findByCodigo(nombre).map(mapper::toDomain);
    }
}
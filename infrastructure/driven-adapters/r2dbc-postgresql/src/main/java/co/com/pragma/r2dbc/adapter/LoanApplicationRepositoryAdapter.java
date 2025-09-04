package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.r2dbc.mapper.LoanApplicationPersistenceMapper;
import co.com.pragma.r2dbc.repository.LoanApplicationDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Override
    public Flux<LoanApplication> findByStatusIn(List<Integer> statusIds, int page, int size, String sortBy, String sortOrder) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return dataRepository.findByStatusIn(statusIds, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByStatusIn(List<Integer> statusIds) {
        return dataRepository.countByStatusIn(statusIds);
    }
}
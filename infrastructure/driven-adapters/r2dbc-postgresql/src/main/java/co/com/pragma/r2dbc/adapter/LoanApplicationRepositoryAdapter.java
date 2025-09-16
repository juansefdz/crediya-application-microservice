package co.com.pragma.r2dbc.adapter;

import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.r2dbc.data.LoanApplicationData;
import co.com.pragma.r2dbc.mapper.LoanApplicationPersistenceMapper;
import co.com.pragma.r2dbc.repository.LoanApplicationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@Slf4j
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
    public Flux<LoanApplication> findByStatusIn(List<String> statusNames, int page, int size, String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return dataRepository.findByStatusIn(statusNames, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByStatusIn(List<String> statusNames) {
        return dataRepository.countByStatusIn(statusNames);
    }

    @Override
    public Mono<LoanApplication> findById(String id) {
        return dataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<LoanApplication> update(LoanApplication solicitud) {
        LoanApplicationData dataEntity = mapper.toData(solicitud);
        dataEntity.markSaved();
        log.info("ADAPTER: Actualizando en BD: {}", dataEntity);
        return dataRepository.save(dataEntity)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<LoanApplication> findAll(int page, int size, String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return dataRepository.findAllBy(pageable).map(mapper::toDomain);
    }
    @Override
    public Flux<LoanApplication> findByUserIdAndStatus(String userId, LoanApplicationStatus status) {
        return dataRepository.findByUsuarioIdAndStatus(userId, status.name())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return dataRepository.count();
    }
}
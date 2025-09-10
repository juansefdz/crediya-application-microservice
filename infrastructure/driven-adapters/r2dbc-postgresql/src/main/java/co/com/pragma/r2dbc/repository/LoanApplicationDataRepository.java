package co.com.pragma.r2dbc.repository;

import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Pageable;
import java.util.List;


public interface LoanApplicationDataRepository extends ReactiveCrudRepository<LoanApplicationData, String> {

    Mono<Long> countByStatusIn(List<Integer> statusIds);

    Flux<LoanApplicationData> findByStatusIn(List<Integer> statusIds, Pageable pageable);

    Mono<Long> count();
    Flux<LoanApplicationData> findAllBy(Pageable pageable);


}
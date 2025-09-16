package co.com.pragma.r2dbc.repository;

import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Pageable;
import java.util.List;


public interface LoanApplicationDataRepository extends ReactiveCrudRepository<LoanApplicationData, String> {

    Mono<Long> countByStatusIn(List<String> status);

    Flux<LoanApplicationData> findByStatusIn(List<String> status, Pageable pageable);

    Mono<Long> count();
    Flux<LoanApplicationData> findAllBy(Pageable pageable);

    Flux<LoanApplicationData> findByUsuarioIdAndStatus(String usuarioId, String status);


}
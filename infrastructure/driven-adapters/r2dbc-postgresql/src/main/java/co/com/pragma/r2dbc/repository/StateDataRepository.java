package co.com.pragma.r2dbc.repository;


import co.com.pragma.r2dbc.data.StateData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface StateDataRepository {
    Mono<StateData> findByNombre(String nombre);
}

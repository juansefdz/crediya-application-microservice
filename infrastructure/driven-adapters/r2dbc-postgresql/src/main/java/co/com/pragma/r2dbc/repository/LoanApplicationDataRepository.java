package co.com.pragma.r2dbc.repository;

import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanApplicationDataRepository extends ReactiveCrudRepository<LoanApplicationData, String> {
}
package co.com.pragma.r2dbc.repository;

import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.awt.print.Pageable;
import java.util.Optional;

public interface LoanApplicationDataRepository extends ReactiveCrudRepository<LoanApplicationData, String> {

}
package co.com.pragma.r2dbc.repository;

import co.com.pragma.r2dbc.data.LoanTypeData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanTypeDataRepository extends ReactiveCrudRepository<LoanTypeData,Long> {
}

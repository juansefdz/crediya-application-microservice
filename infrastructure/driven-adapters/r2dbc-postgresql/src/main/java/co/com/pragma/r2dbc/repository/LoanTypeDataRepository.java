package co.com.pragma.r2dbc.repository;



import co.com.pragma.r2dbc.data.LoanTypeData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanTypeDataRepository extends ReactiveCrudRepository<LoanTypeData,Long> {
}

package co.com.pragma.model.userdata.gateways;

import co.com.pragma.model.userdata.UserData;
import reactor.core.publisher.Mono;

public interface UserDataRepository {

    Mono<UserData> findUserFinancialDataById(String userId);
}

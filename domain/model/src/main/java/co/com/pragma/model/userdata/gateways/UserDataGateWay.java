package co.com.pragma.model.userdata.gateways;

import co.com.pragma.model.userdata.UserData;
import reactor.core.publisher.Mono;

public interface UserDataGateWay {

    Mono<UserData> findUserFinancialDataById(String userId);
}

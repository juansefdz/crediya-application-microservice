package co.com.pragma.lambda.debtcapacoty;

import co.com.pragma.model.userdata.UserData;
import co.com.pragma.model.userdata.gateways.UserDataGateWay;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;


@RequiredArgsConstructor
public class HttpUserDataAdapter implements UserDataGateWay {
    private final WebClient webClient;

    @Override
    public Mono<UserData> findUserFinancialDataById(String userId) {
        return webClient.get().uri("/api/v1/internal/users/{id}/financial-profile", userId).retrieve().bodyToMono(UserData.class);
    }
}
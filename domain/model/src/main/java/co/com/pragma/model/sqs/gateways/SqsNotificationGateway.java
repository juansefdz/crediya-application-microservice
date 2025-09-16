package co.com.pragma.model.sqs.gateways;
import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Mono;

public interface SqsNotificationGateway {

    Mono<Void> sendMessageForValidation(LoanApplication application);

}

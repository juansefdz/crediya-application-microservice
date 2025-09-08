package co.com.pragma.model.notification.gateways;

import co.com.pragma.model.loanapplication.LoanApplication;
import reactor.core.publisher.Mono;

public interface NotificationRepository {

    Mono<Void> sendNotificationCreditReport(LoanApplication solicitud);
}
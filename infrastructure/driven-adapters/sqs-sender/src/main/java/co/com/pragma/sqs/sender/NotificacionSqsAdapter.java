package co.com.pragma.sqs.sender;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.notification.gateways.NotificationRepository;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionSqsAdapter implements NotificationRepository {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper; 

    @Override
    public Mono<Void> sendNotificationCreditReport(LoanApplication solicitud) {
        return Mono.fromCallable(() -> {
                    var messageDto = new NotificacionMessageDTO(
                            solicitud.getUsuarioId(),
                            solicitud.getEmail(),
                            solicitud.getStatus().name(),
                            solicitud.getId()
                    );
                    return objectMapper.writeValueAsString(messageDto); // 👈 serialize
                })
                .flatMap(this::send)
                .then();
    }

    private Mono<String> send(String jsonMessage) {
        return Mono.fromCallable(() -> buildRequest(jsonMessage))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnSuccess(response -> log.info("Mensaje enviado a SQS con éxito. MessageId: {}", response.messageId()))
                .doOnError(e -> log.error("Error al enviar mensaje a SQS: ", e))
                .map(response -> response.messageId());
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

    private record NotificacionMessageDTO(String usuarioId, String email, String estado, String solicitudId) {}
}

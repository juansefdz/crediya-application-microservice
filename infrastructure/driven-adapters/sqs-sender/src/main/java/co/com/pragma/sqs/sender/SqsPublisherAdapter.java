package co.com.pragma.sqs.sender;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.sqs.gateways.SqsNotificationGateway;
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
public class SqsPublisherAdapter implements SqsNotificationGateway {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendMessageForValidation(LoanApplication application) {
        return Mono.fromCallable(() -> {

                    var messageDto = new ValidationMessageDTO(
                            application.getId(),
                            application.getUsuarioId()
                    );
                    return objectMapper.writeValueAsString(messageDto);
                })
                .flatMap(this::send)
                .then();
    }

    private Mono<String> send(String jsonMessage) {
        return Mono.fromCallable(() -> buildRequest(jsonMessage))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnSuccess(response -> log.info("Mensaje de validación enviado a SQS. MessageId: {}", response.messageId()))
                .doOnError(e -> log.error("Error al enviar mensaje de validación a SQS: ", e))
                .map(response -> response.messageId());
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.validationQueueUrl()) // <-- Apunta a la cola de validación
                .messageBody(message)
                .build();
    }

    private record ValidationMessageDTO(String loanApplicationId, String userId) {}
}
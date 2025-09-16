package co.com.pragma.lambda.debtcapacoty;

import co.com.pragma.model.customExceptions.BusinessException;
import co.com.pragma.model.financial.gateways.BusinessRulesGateway;
import co.com.pragma.model.financial.gateways.FinancialCalculatorGateway;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.userdata.gateways.UserDataGateWay;
import co.com.pragma.usecase.debtcapacity.DebtCapacityUseCase;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class DebtCapacityHandler implements RequestHandler<SQSEvent, String> {

    private final DebtCapacityUseCase debtCapacityUseCase;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;


    public DebtCapacityHandler() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String microserviceBaseUrl = System.getenv("MICROSERVICE_BASE_URL");
        this.webClient = WebClient.builder().baseUrl(microserviceBaseUrl).build();

        LoanApplicationRepository loanRepoAdapter = new HttpHttpLoanApplicationAdapter(this.webClient);
        UserDataGateWay userDataGatewayAdapter = new HttpUserDataAdapter(this.webClient);
        FinancialCalculatorGateway calculatorAdapter = new FinancialCalculatorAdapter();
        BusinessRulesGateway rulesAdapter = new BusinessRulesAdapter();

        this.debtCapacityUseCase = new DebtCapacityUseCase(loanRepoAdapter, userDataGatewayAdapter, calculatorAdapter, rulesAdapter);
    }


    DebtCapacityHandler(DebtCapacityUseCase debtCapacityUseCase, ObjectMapper objectMapper, WebClient webClient) {
        this.debtCapacityUseCase = debtCapacityUseCase;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("Lambda invocada con {} mensaje(s).", sqsEvent.getRecords().size());

        for (SQSEvent.SQSMessage msg : sqsEvent.getRecords()) {
            try {
                String messageBody = msg.getBody();
                ValidationMessageDTO messageDto = objectMapper.readValue(messageBody, ValidationMessageDTO.class);
                String loanApplicationId = messageDto.loanApplicationId();
                log.info("Procesando solicitud ID: {}", loanApplicationId);

                debtCapacityUseCase.execute(loanApplicationId)
                        .flatMap(result -> {
                            log.info("Validación completada para {}. Resultado: {}", loanApplicationId, result.getNewStatus());
                            return updateApplicationStatus(loanApplicationId, result.getNewStatus().name());
                        })
                        .doOnError(BusinessException.class, e -> log.warn("Fallo de negocio controlado [{}]: {}", e.getCode(), e.getMessage()))
                        .doOnError(e -> !(e instanceof BusinessException), e -> log.error("Error no controlado procesando solicitud {}", loanApplicationId, e))
                        .block();

            } catch (Exception e) {
                log.error("Error crítico al procesar mensaje SQS. MessageId: {}", msg.getMessageId(), e);
                throw new RuntimeException("No se pudo procesar el mensaje " + msg.getMessageId(), e);
            }
        }
        return "Proceso completado.";
    }

    private Mono<Void> updateApplicationStatus(String id, String newStatus) {
        String callbackUrl = "/api/v1/internal/solicitudes/{id}/status";
        var body = new UpdateStatusCallbackDTO(newStatus);

        log.info("Enviando callback a {} con estado {}", callbackUrl.replace("{id}", id), newStatus);

        return webClient.put()
                .uri(callbackUrl, id)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .then();
    }


    private record ValidationMessageDTO(String loanApplicationId, String userId) {}
    private record UpdateStatusCallbackDTO(String nuevoEstado) {}
}
package co.com.pragma.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
        String region,
        String endpoint,
        String validationQueueUrl,  // validación
        String notificationQueueUrl //notificaciones
){
}

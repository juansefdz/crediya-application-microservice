package co.com.pragma.api.exceptions;

import co.com.pragma.api.dto.ErrorResponseDTO;
import co.com.pragma.model.customExceptions.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final Map<Class<? extends Throwable>, Function<Throwable, ErrorResponseDTO>> exceptionHandlers = new HashMap<>();

    @PostConstruct
    public void init() {
        // Registro de excepciones de negocio personalizadas

        // Registro de excepciones del framework
        exceptionHandlers.put(ServerWebInputException.class, this::handleServerWebInputException);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("GlobalExceptionHandler caught an error. Exception Type: {}", ex.getClass().getName(), ex);

        ErrorResponseDTO errorResponse = exceptionHandlers.getOrDefault(ex.getClass(), this::defaultErrorHandler).apply(ex);

        errorResponse.setPath(exchange.getRequest().getPath().value());

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(errorResponse.getStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = bufferFactory.wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON response", e);
            return Mono.empty();
        }
    }

    private ErrorResponseDTO handleServerWebInputException(Throwable ex) {
        ServerWebInputException webInputEx = (ServerWebInputException) ex;
        String message;

        Throwable cause = webInputEx.getRootCause();
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ifx) {
            String fieldName = ifx.getPath().isEmpty() ? "unknown" : ifx.getPath().get(0).getFieldName();
            message = String.format("El valor '%s' no es válido para el campo '%s'.", ifx.getValue(), fieldName);
        }

        else if (webInputEx.getCause() instanceof WebExchangeBindException bindEx) {
            message = bindEx.getAllErrors().stream()
                    .map(error -> {
                        String field = (error instanceof org.springframework.validation.FieldError fieldError)
                                ? fieldError.getField() : error.getObjectName();
                        return field + ": " + error.getDefaultMessage();
                    })
                    .collect(Collectors.joining(", "));
        }
        else {
            message = "La petición tiene un formato inválido. Por favor, revise los datos enviados.";
        }

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, "Bad Request", "BAD_REQUEST");
    }

    private ErrorResponseDTO buildErrorResponse(Throwable ex, HttpStatus status, String errorType) {
        BusinessException bex = (BusinessException) ex;
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .code(bex.getCode())
                .error(errorType)
                .message(bex.getMessage())
                .build();
    }

    private ErrorResponseDTO buildErrorResponse(String message, HttpStatus status, String errorType, String errorCode) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .code(errorCode)
                .error(errorType)
                .message(message)
                .build();
    }

    private ErrorResponseDTO defaultErrorHandler(Throwable ex) {
        return buildErrorResponse("Ocurrió un error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "INTERNAL_ERROR");
    }
}
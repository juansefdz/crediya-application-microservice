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
import org.springframework.validation.FieldError;
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
    private final Map<Class<? extends BusinessException>, HttpStatus> statusMap = new HashMap<>();

    @PostConstruct
    public void init() {
        exceptionHandlers.put(BusinessException.class, this::handleBusinessException);
        exceptionHandlers.put(ServerWebInputException.class, this::handleServerWebInputException);

        statusMap.put(LoanTypeNotFoundException.class, HttpStatus.NOT_FOUND);
        statusMap.put(InvalidLoanApplicationException.class, HttpStatus.BAD_REQUEST);
        statusMap.put(LoanApplicationNotFoundException.class, HttpStatus.NOT_FOUND);
        statusMap.put(InvalidStatusException.class, HttpStatus.BAD_REQUEST);
        statusMap.put(InvalidPaginationException.class, HttpStatus.BAD_REQUEST);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("GlobalExceptionHandler caught an error. Exception Type: {}", ex.getClass().getName(), ex);

        Function<Throwable, ErrorResponseDTO> handler = findHandler(ex.getClass());
        ErrorResponseDTO errorResponse = handler.apply(ex);
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

    private Function<Throwable, ErrorResponseDTO> findHandler(Class<?> exceptionClass) {
        if (exceptionClass == null) {
            return this::defaultErrorHandler;
        }
        Function<Throwable, ErrorResponseDTO> handler = exceptionHandlers.get(exceptionClass);
        if (handler != null) {
            return handler;
        }
        return findHandler(exceptionClass.getSuperclass());
    }

    private ErrorResponseDTO handleBusinessException(Throwable ex) {
        BusinessException bex = (BusinessException) ex;
        HttpStatus status = statusMap.getOrDefault(bex.getClass(), HttpStatus.BAD_REQUEST);
        return buildErrorResponse(bex, status);
    }

    private ErrorResponseDTO handleServerWebInputException(Throwable ex) {
        ServerWebInputException webInputEx = (ServerWebInputException) ex;
        String message = "La petición tiene un formato inválido.";
        Map<String, String> errors = null;

        if (webInputEx.getCause() instanceof WebExchangeBindException bindEx) {
            message = "Error de validación. Por favor, revise los campos.";
            errors = bindEx.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Valor inválido"
                    ));
        }

        ErrorResponseDTO response = buildErrorResponse(message, HttpStatus.BAD_REQUEST, "Bad Request", "BAD_REQUEST");
        // Asumiendo que ErrorResponseDTO tiene un campo para validationErrors
        // response.setValidationErrors(errors);
        return response;
    }

    private ErrorResponseDTO buildErrorResponse(BusinessException bex, HttpStatus status) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .code(bex.getCode())
                .error(status.getReasonPhrase())
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
        log.error("Unhandled exception occurred: ", ex);
        return buildErrorResponse("Ocurrió un error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "INTERNAL_ERROR");
    }
}
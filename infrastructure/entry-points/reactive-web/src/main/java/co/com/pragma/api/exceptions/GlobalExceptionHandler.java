package co.com.pragma.api.exceptions;

import co.com.pragma.api.dto.ErrorResponseDTO;
import co.com.pragma.model.ErrorCode;
import co.com.pragma.model.customExceptions.BusinessException;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final Map<ErrorCode, HttpStatus> statusMap = new EnumMap<>(ErrorCode.class);

    @PostConstruct
    public void init() {
        // --- MAPA DE TRADUCCIÓN: ErrorCode -> HttpStatus ---
        // Errores de Validación
        statusMap.put(ErrorCode.VAL_AMOUNT_INVALID, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_TERM_INVALID, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_LOAN_TYPE_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_USER_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_LOAN_TYPE_ID_FORMAT, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_AMOUNT_MIN_EXCEEDED, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_AMOUNT_MAX_EXCEEDED, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_PAGINATION_INVALID, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_INSUFFICIENT_INCOME, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_EXCESSIVE_DEBT_RATIO, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_REQUIRES_MANUAL_REVIEW, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_INSUFFICIENT_DEBT_CAPACITY, HttpStatus.BAD_REQUEST);
        statusMap.put(ErrorCode.VAL_FINANCIAL_DATA_INVALID, HttpStatus.BAD_REQUEST);

        // Errores de Autenticación/Autorización
        statusMap.put(ErrorCode.AUTH_UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        statusMap.put(ErrorCode.AUTH_FORBIDDEN, HttpStatus.FORBIDDEN);

        // Errores de No Encontrado
        statusMap.put(ErrorCode.APP_NOT_FOUND, HttpStatus.NOT_FOUND);
        statusMap.put(ErrorCode.LTY_NOT_FOUND, HttpStatus.NOT_FOUND);
        statusMap.put(ErrorCode.USER_DATA_NOT_FOUND, HttpStatus.NOT_FOUND);

        // Errores de Conflicto
        statusMap.put(ErrorCode.APP_STATE_CONFLICT, HttpStatus.CONFLICT);

        // Errores de Servidor
        statusMap.put(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        statusMap.put(ErrorCode.SYS_CALCULATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        statusMap.put(ErrorCode.SYS_PLAN_GENERATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("GlobalExceptionHandler caught an error: {}", ex.getMessage());

        ErrorResponseDTO errorResponse = buildErrorResponse(ex);
        errorResponse.setPath(exchange.getRequest().getPath().value());

        HttpStatus httpStatus = determineHttpStatus(ex);
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return writeResponse(exchange, errorResponse);
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof BusinessException bex) {
            // Usa el mapa para encontrar el HttpStatus a partir del ErrorCode
            return statusMap.getOrDefault(bex.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (ex instanceof WebExchangeBindException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ErrorResponseDTO buildErrorResponse(Throwable ex) {
        if (ex instanceof BusinessException bex) {
            HttpStatus status = determineHttpStatus(bex);
            return ErrorResponseDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status.value())
                    .code(bex.getErrorCode().getCode())
                    .error(status.getReasonPhrase())
                    .message(bex.getMessage())
                    .build();
        }
        if (ex instanceof WebExchangeBindException bindEx) {
            Map<String, String> errors = bindEx.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Valor inválido"
                    ));
            ErrorResponseDTO dto = buildErrorResponse("Error de validación", HttpStatus.BAD_REQUEST, "BAD_REQUEST", "VAL-400");
            dto.setValidationErrors(errors);
            return dto;
        }

        log.error("Unhandled exception occurred: ", ex);
        return buildErrorResponse("Ocurrió un error inesperado.", HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
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

    private Mono<Void> writeResponse(ServerWebExchange exchange, ErrorResponseDTO errorResponse) {
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
}
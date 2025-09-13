package co.com.pragma.api.exceptions;

import co.com.pragma.api.dto.ErrorResponseDTO;
import co.com.pragma.model.customExceptions.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-1)
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler implements WebFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(BusinessException.class, ex -> {
                    log.error("BusinessException capturada en el filtro: {}", ex.getCode());
                    return handleBusinessException(exchange, ex);
                })
                .onErrorResume(throwable -> {
                    log.error("⚠Otra excepción en el filtro: {}", throwable.getClass().getSimpleName());

                    return Mono.error(throwable);
                });
    }

    private Mono<Void> handleBusinessException(ServerWebExchange exchange, BusinessException ex) {
        try {
            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .code(ex.getCode())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .message(ex.getMessage())
                    .path(exchange.getRequest().getPath().value())
                    .build();

            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String json = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(json.getBytes());

            log.info("BusinessException manejada en filtro exitosamente");
            return exchange.getResponse().writeWith(Mono.just(buffer));

        } catch (Exception e) {
            log.error("Error en BusinessExceptionFilter", e);
            return Mono.error(ex);
        }
    }
}
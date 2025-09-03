package co.com.pragma.api.config;

import co.com.pragma.api.Handler;
import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    @RouterOperations(
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    // ¡CORRECCIÓN CLAVE! El método en tu Handler se llama "createLoanApplication"
                    beanMethod = "createLoanApplication",
                    operation = @Operation(
                            operationId = "createLoanApplication",
                            summary = "Crear una nueva solicitud de préstamo",
                            description = "Crea una nueva solicitud de préstamo en el sistema.",
                            tags = { "Solicitudes de Préstamo" },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos para la nueva solicitud de préstamo",
                                    content = @Content(schema = @Schema(implementation = LoanApplicationRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Solicitud creada exitosamente",
                                            content = @Content(schema = @Schema(implementation = LoanApplicationResponseDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    )
    public RouterFunction<ServerResponse> loanApplicationRoute(Handler handler) {
        return route(POST("/api/v1/solicitud").and(accept(MediaType.APPLICATION_JSON)), handler::createLoanApplication);
    }
}
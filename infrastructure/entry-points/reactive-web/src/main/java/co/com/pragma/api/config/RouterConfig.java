package co.com.pragma.api.config;

import co.com.pragma.api.Handler;
import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationResponseDTO;
import co.com.pragma.api.dto.LoanApplicationSummaryDTO;
import co.com.pragma.api.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    private static final String API_PATH = "/api/v1/solicitudes";

    @Bean
    @RouterOperations({

            @RouterOperation(
                    path = API_PATH,
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createLoanApplication",
                    operation = @Operation( /* ... Tu operación POST ... */ )
            ),

            @RouterOperation(
                    path = API_PATH,
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "obtenerSolicitudes",
                    operation = @Operation(
                            operationId = "obtenerSolicitudes",
                            summary = "Obtener listado paginado de solicitudes para revisión",
                            tags = { "Solicitudes de Préstamo" },
                            parameters = {
                                    @Parameter(in = ParameterIn.QUERY, name = "page", description = "Número de página (inicia en 0)", schema = @Schema(type = "integer", defaultValue = "0")),
                                    @Parameter(in = ParameterIn.QUERY, name = "size", description = "Tamaño de la página", schema = @Schema(type = "integer", defaultValue = "10")),
                                    @Parameter(in = ParameterIn.QUERY, name = "sortBy", description = "Campo para ordenar", schema = @Schema(type = "string", defaultValue = "monto")),
                                    @Parameter(in = ParameterIn.QUERY, name = "sortOrder", description = "Orden (ASC o DESC)", schema = @Schema(type = "string", defaultValue = "ASC"))
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Operación exitosa",
                                            content = @Content(schema = @Schema(implementation = PageResponseDTO.class)) // Nota: Aquí referenciamos el DTO de paginación
                                    ),
                                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                                    @ApiResponse(responseCode = "403", description = "Acceso prohibido")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> loanApplicationRoutes(Handler handler) {
        return route(POST(API_PATH).and(accept(MediaType.APPLICATION_JSON)), handler::createLoanApplication)
                .andRoute(GET(API_PATH), handler::obtenerSolicitudes);
    }
}
package co.com.pragma.api.config;

import co.com.pragma.api.Handler;
import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationResponseDTO;
import co.com.pragma.api.dto.PageResponseDTO;
import co.com.pragma.api.dto.UpdateStatusRequestDTO;
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

    private static final String API_PATH_PLURAL = "/api/v1/solicitudes";
    private static final String API_PATH_SINGULAR_ID = "/api/v1/solicitud/{id}";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = API_PATH_PLURAL,
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createLoanApplication",
                    operation = @Operation(
                            operationId = "createLoanApplication",
                            summary = "Crear una nueva solicitud de préstamo",
                            tags = { "Solicitudes de Préstamo" },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos de la nueva solicitud de préstamo",
                                    content = @Content(schema = @Schema(implementation = LoanApplicationRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = LoanApplicationResponseDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
                            }
                    )
            ),
            @RouterOperation(
                    path = API_PATH_PLURAL,
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
                                            content = @Content(schema = @Schema(implementation = PageResponseDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                                    @ApiResponse(responseCode = "403", description = "Acceso prohibido")
                            }
                    )
            ),
            @RouterOperation(
                    path = API_PATH_SINGULAR_ID,
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updateApplicationStatus",
                    operation = @Operation(
                            operationId = "updateApplicationStatus",
                            summary = "Aprobar o Rechazar una Solicitud de Crédito",
                            tags = { "Solicitudes de Préstamo" },
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "id", description = "ID de la solicitud a actualizar", required = true, schema = @Schema(type = "string"))
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "DTO con el nuevo estado para la solicitud ('APROBADA' o 'RECHAZADA')",
                                    content = @Content(schema = @Schema(implementation = UpdateStatusRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud actualizada exitosamente", content = @Content(schema = @Schema(implementation = LoanApplicationResponseDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. estado no permitido)"),
                                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                                    @ApiResponse(responseCode = "403", description = "Acceso prohibido (rol no es Asesor)"),
                                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> loanApplicationRoutes(Handler handler) {
        return route(POST(API_PATH_PLURAL).and(accept(MediaType.APPLICATION_JSON)), handler::createLoanApplication)
                .andRoute(GET(API_PATH_PLURAL), handler::obtenerSolicitudes)
                .andRoute(PUT(API_PATH_SINGULAR_ID).and(accept(MediaType.APPLICATION_JSON)), handler::updateApplicationStatus);
    }
}
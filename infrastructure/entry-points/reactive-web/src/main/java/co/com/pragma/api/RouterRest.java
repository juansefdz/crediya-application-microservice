package co.com.pragma.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterRest {

    @Bean
    public RouterFunction<ServerResponse> routes(Handler handler) {
        return RouterFunctions
                .route(POST("/loan-application").and(accept(MediaType.APPLICATION_JSON)), handler::createLoanApplication)
                .andRoute(GET("/api/v1/solicitudes/in-review"), handler::obtenerSolicitudes)
                .andRoute(GET("/api/v1/solicitudes"), handler::listAllApplications)
                .andRoute(PUT("/api/v1/solicitud/{id}"), handler::updateApplicationStatus);
    }
}

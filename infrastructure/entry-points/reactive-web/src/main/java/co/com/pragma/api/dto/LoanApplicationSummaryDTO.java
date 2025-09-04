package co.com.pragma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoanApplicationSummaryDTO {

    @Schema(description = "ID único de la solicitud.", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private String id;

    @Schema(description = "Número de cédula del cliente.", example = "12345678")
    private String usuarioId;

    @Schema(description = "Correo electrónico del cliente.", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "Monto del préstamo solicitado.", example = "100000")
    private BigDecimal monto;

    @Schema(description = "Plazo del préstamo en meses.", example = "24")
    private Integer plazo;

    @Schema(description = "Id del tipo de préstamo.", example = "1")
    private String prestamoId;

    @Schema(description = "Estado actual de la solicitud.", example = "PENDIENTE_REVISION")
    private String estadoSolicitud;
}
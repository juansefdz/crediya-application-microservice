package co.com.pragma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequestDTO {

    @Schema(example = "12345678", description = "número de cédula del cliente")
    @Size(min = 5, max = 20, message = "El documento debe tener entre 5 y 20 caracteres")
    @NotBlank(message = "El ID del usuario no puede estar vacío.")
    private String usuarioId;
    @Schema(example = "juan.perez@example.com")
    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    private String email;
    @Schema(example = "100000")
    @NotNull(message = "El monto no puede ser nulo.")
    @Positive(message = "El monto debe ser un valor positivo.")
    private BigDecimal monto;
    @Schema(example= "24", description = "el plazo es de (24) meses")
    @NotNull(message = "El plazo no puede ser nulo.")
    @Positive(message = "El plazo debe ser de al menos 1 mes.")
    private Integer plazo;
    @Schema(example = "1", description = "Id del tipo de préstamo. Ej: 1 para Crédito de Libre Inversión")
    @NotBlank(message = "El ID del tipo de préstamo no puede estar vacío.")
    private String prestamoId;
}
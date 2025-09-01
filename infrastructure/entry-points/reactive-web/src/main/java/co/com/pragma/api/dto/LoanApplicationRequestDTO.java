package co.com.pragma.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequestDTO {

    @NotBlank(message = "El ID del usuario no puede estar vacío.")
    private String usuarioId;

    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El formato del email no es válido.")
    private String email;

    @NotNull(message = "El monto no puede ser nulo.")
    @Positive(message = "El monto debe ser un valor positivo.")
    private BigDecimal monto;

    @NotNull(message = "El plazo no puede ser nulo.")
    @Positive(message = "El plazo debe ser de al menos 1 mes.")
    private Integer plazo;

    @NotBlank(message = "El ID del tipo de préstamo no puede estar vacío.")
    private String prestamoId;
}
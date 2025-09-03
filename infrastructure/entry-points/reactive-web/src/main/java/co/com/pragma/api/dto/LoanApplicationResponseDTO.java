package co.com.pragma.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponseDTO {

    private String id;
    private String usuarioId;
    private BigDecimal monto;
    private Integer plazo;
    private String status;
    private String prestamoId;
}
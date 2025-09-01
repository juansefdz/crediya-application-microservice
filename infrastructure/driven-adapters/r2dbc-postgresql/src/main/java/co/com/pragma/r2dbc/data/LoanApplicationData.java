package co.com.pragma.r2dbc.data;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Table("solicitud")
public class LoanApplicationData {

        @Id
        @Column("id_solicitud")
        private String id;
        @Column("monto")
        private BigDecimal monto;
        @Column("plazo")
        private Integer plazo;
        @Column("email")
        private String customerId;
        @Column("id_estado")
        private Integer idEstado;
        @Column("id_tipo_prestamo")
        private String idTipoPrestamo;
}
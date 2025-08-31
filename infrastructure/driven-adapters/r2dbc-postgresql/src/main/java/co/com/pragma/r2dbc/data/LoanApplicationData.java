package co.com.pragma.r2dbc.data;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("solicitud")
public class LoanApplicationData {
    @Id
    @Column("id_solicitud")
    private Long idSolicitud;

    @Column("documento_cliente")
    private String documentoCliente;

    private String email;
    private Long monto;
    private Integer plazo;

    @Column("id_estado")
    private Long idEstado;

    @Column("id_tipo_prestamo")
    private Long idTipoPrestamo;
}
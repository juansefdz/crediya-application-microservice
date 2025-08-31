package co.com.pragma.r2dbc.data;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("tipo_prestamo")
public class LoanTypeData {
    @Id
    @Column("id_tipo_prestamo")
    private Long idTipoPrestamo;
    private String nombre;
    @Column("monto_minimo")
    private Long montoMinimo;
    @Column("monto_maximo")
    private Long montoMaximo;
    @Column("tasa_interes")
    private Double tasaInteres;
    @Column("validacion_automatica")
    private Boolean validacionAutomatica;
}
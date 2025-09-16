package co.com.pragma.r2dbc.data;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Table("tipo_prestamo")
public class LoanTypeData {
    @Id
    @Column("id_tipo_prestamo")
    private String id;
    @Column("nombre")
    private String nombre;
    @Column("monto_minimo")
    private BigDecimal montoMinimo;
    @Column("monto_maximo")
    private BigDecimal montoMaximo;
    @Column("tasa_interes")
    private Double tasaInteres;
    @Column("validacion_automatica")
    private Boolean automaticValidation;
}
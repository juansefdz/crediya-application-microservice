package co.com.pragma.r2dbc.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
@Data
@NoArgsConstructor
@Table("estado")
public class StateData {
        @Id
        @Column("id_estado")
        private Integer id;

        @Column("nombre")
        private String nombre;

        @Column("codigo")
        private String codigo;
}
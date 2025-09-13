package co.com.pragma.r2dbc.data;

import co.com.pragma.model.LoanApplicationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@Table("solicitud")
public class LoanApplicationData implements Persistable<String> {

        @Id
        @Column("id_solicitud")
        private String id;


        @Column("documento_cliente")
        private String userId;

        @Column("email")
        private String email;

        @Column("monto")
        private BigDecimal monto;

        @Column("plazo")
        private Integer plazo;


        @Column("id_tipo_prestamo")
        private String prestamoId;


        @Column("id_estado")
        private Integer status;

        @Transient
        private boolean newEntity = true;

        @Override public String getId() { return id; }
        @Override public boolean isNew() { return newEntity; }

        public void markNew()   { this.newEntity = true; }
        public void markSaved() { this.newEntity = false; }
}

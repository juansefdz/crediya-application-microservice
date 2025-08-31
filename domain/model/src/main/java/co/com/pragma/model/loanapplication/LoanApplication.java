package co.com.pragma.model.loanapplication;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanApplication {

    private Long id;
    private String documentoCliente;
    private String email;
    private Long monto;
    private Integer plazo;
    private Long estadoId;
    private Long tipoPrestamoId;
}

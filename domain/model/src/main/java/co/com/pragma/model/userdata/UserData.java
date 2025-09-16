package co.com.pragma.model.userdata;
import lombok.Builder;
import lombok.Data;


import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class UserData {
    private String id;
    private BigDecimal totalIncome;

}
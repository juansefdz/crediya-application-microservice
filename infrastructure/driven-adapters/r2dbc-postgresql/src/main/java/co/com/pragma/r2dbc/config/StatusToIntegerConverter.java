package co.com.pragma.r2dbc.config; // O un paquete de convertidores

import co.com.pragma.model.LoanApplicationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class StatusToIntegerConverter implements Converter<LoanApplicationStatus, Integer> {
    @Override
    public Integer convert(LoanApplicationStatus source) {
        return source.getId();
    }
}
package co.com.pragma.r2dbc.config;

import co.com.pragma.model.LoanApplicationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class IntegerToStatusConverter implements Converter<Integer, LoanApplicationStatus> {
    @Override
    public LoanApplicationStatus convert(Integer source) {
        return LoanApplicationStatus.fromId(source);
    }
}
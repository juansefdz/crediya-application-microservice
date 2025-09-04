package co.com.pragma.r2dbc.converter;

import co.com.pragma.model.LoanApplicationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter // indica que es para leer desde la DB
public class LongToLoanApplicationStatusConverter implements Converter<Long, LoanApplicationStatus> {

    @Override
    public LoanApplicationStatus convert(Long source) {
        return LoanApplicationStatus.fromId(source.intValue());
    }
}
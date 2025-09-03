package co.com.pragma.r2dbc.config;

import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoanApplicationBeforeConvertCallback
        implements BeforeConvertCallback<LoanApplicationData> {

    @Override
    public LoanApplicationData onBeforeConvert(LoanApplicationData entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
        }
        return entity;
    }
}

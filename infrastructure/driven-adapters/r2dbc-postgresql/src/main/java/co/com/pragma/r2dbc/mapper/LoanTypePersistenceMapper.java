package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.r2dbc.data.LoanTypeData;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanTypePersistenceMapper {

    LoanType toDomain(LoanTypeData data);

    @InheritInverseConfiguration
    LoanTypeData toData(LoanType domain);
}
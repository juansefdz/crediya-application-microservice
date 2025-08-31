package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.loanstate.LoanState;
import co.com.pragma.r2dbc.data.StateData;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StateDataPersistenceMapper {

    LoanState toDomain(StateData data);

    @InheritInverseConfiguration
    StateData toData(LoanState domain);
}
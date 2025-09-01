package co.com.pragma.r2dbc.mapper;


import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationPersistenceMapper {

    @Mapping(target = "usuarioId", source = "customerId")
    @Mapping(target = "prestamoId", source = "idTipoPrestamo")
    @Mapping(target = "estadoId", source = "idEstado")

    LoanApplication toDomain(LoanApplicationData data);


    @Mapping(target = "customerId", source = "usuarioId")
    @Mapping(target = "idTipoPrestamo", source = "prestamoId")
    @Mapping(target = "idEstado", source = "estadoId")

    LoanApplicationData toData(LoanApplication domain);
}

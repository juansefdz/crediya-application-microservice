package co.com.pragma.r2dbc.mapper;


import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface LoanApplicationPersistenceMapper {

    @Mappings({
            @Mapping(target = "id",              source = "idSolicitud"),
            @Mapping(target = "estadoId",        source = "idEstado"),
            @Mapping(target = "tipoPrestamoId",  source = "idTipoPrestamo"),
            @Mapping(target = "documentoCliente", source = "documentoCliente"),
            @Mapping(target = "email",            source = "email"),
            @Mapping(target = "monto",            source = "monto"),
            @Mapping(target = "plazo",            source = "plazo")
    })
    LoanApplication toDomain(LoanApplicationData data);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(target = "idSolicitud",     source = "id"),
            @Mapping(target = "idEstado",        source = "estadoId"),
            @Mapping(target = "idTipoPrestamo",  source = "tipoPrestamoId")
    })
    LoanApplicationData toData(LoanApplication domain);
}
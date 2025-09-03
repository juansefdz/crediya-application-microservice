package co.com.pragma.r2dbc.mapper;


import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanApplicationPersistenceMapper {

    @Mappings({
            @Mapping(target = "id",               source = "id"),
            @Mapping(target = "customerDocument", source = "usuarioId"),
            @Mapping(target = "email",            source = "email"),
            @Mapping(target = "monto",            source = "monto"),
            @Mapping(target = "plazo",            source = "plazo"),
            @Mapping(target = "idTipoPrestamo",
                    expression = "java(domain.getPrestamoId() == null ? null : Long.valueOf(domain.getPrestamoId()))"),
            @Mapping(target = "status",           source = "status")
    })
    LoanApplicationData toData(LoanApplication domain);

    @AfterMapping
    default void forceInsert(@MappingTarget LoanApplicationData target) {
        target.markNew();
    }

    @Mapping(target = "usuarioId", source = "customerDocument")
    @Mapping(target = "prestamoId",
            expression = "java(data.getIdTipoPrestamo() == null ? null : String.valueOf(data.getIdTipoPrestamo()))")
    LoanApplication toDomain(LoanApplicationData data);
}
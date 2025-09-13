package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanApplicationPersistenceMapper {

    @Mapping(target = "status", expression = "java(domain.getStatus() == null ? null : domain.getStatus().getId())")
    @Mapping(target = "userId", source = "usuarioId")
    LoanApplicationData toData(LoanApplication domain);

    @Mapping(target = "status", expression = "java(data.getStatus() == null ? null : co.com.pragma.model.LoanApplicationStatus.fromId(data.getStatus()))")
    @Mapping(target = "usuarioId", source = "userId")
    LoanApplication toDomain(LoanApplicationData data);
}

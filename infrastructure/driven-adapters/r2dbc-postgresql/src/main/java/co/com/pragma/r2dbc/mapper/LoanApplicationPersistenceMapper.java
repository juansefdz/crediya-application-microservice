package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.LoanApplicationStatus;
import co.com.pragma.r2dbc.data.LoanApplicationData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = LoanApplicationStatus.class)
public interface LoanApplicationPersistenceMapper {

    @Mapping(target = "status", expression = "java(domain.getStatus() == null ? null : domain.getStatus().name())")
    LoanApplicationData toData(LoanApplication domain);

    @Mapping(target = "status", expression = "java(data.getStatus() == null ? null : LoanApplicationStatus.valueOf(data.getStatus()))")
    LoanApplication toDomain(LoanApplicationData data);
}
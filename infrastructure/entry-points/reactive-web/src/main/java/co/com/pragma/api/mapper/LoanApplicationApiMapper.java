package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationResponseDTO;
import co.com.pragma.api.dto.LoanApplicationSummaryDTO;
import co.com.pragma.model.loanapplication.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanApplicationApiMapper {

    @Mapping(target = "id",     expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", expression = "java(co.com.pragma.model.LoanApplicationStatus.PENDIENTE_REVISION)")
    LoanApplication toDomain(LoanApplicationRequestDTO dto);

    LoanApplicationResponseDTO toDTO(LoanApplication domain);

    //para el listado
    @Mapping(source = "status", target = "estadoSolicitud")
    LoanApplicationSummaryDTO toSummaryDTO(LoanApplication domain);
}
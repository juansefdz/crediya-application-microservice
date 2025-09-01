package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.LoanApplicationRequestDTO;
import co.com.pragma.api.dto.LoanApplicationResponseDTO;
import co.com.pragma.model.loanapplication.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationApiMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    LoanApplication toDomain(LoanApplicationRequestDTO dto);

    LoanApplicationResponseDTO toDTO(LoanApplication domain);
}
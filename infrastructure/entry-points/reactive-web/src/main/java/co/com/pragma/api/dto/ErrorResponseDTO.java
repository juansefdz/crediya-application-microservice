package co.com.pragma.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
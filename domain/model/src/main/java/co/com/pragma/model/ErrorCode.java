package co.com.pragma.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- ERRORES DE VALIDACIÓN (400 Bad Request) ---
    VAL_AMOUNT_INVALID("VAL-001", "El monto de la solicitud debe ser mayor a cero."),
    VAL_TERM_INVALID("VAL-002", "El plazo debe ser de al menos un mes."),
    VAL_LOAN_TYPE_ID_REQUIRED("VAL-003", "El tipo de préstamo es obligatorio."),
    VAL_USER_ID_REQUIRED("VAL-004", "La identificación del usuario es obligatoria."),
    VAL_LOAN_TYPE_ID_FORMAT("VAL-005", "El ID del tipo de préstamo no es un número válido: %s"),
    VAL_AMOUNT_MIN_EXCEEDED("VAL-006", "El monto solicitado es menor al mínimo permitido, debe ser mayor a 0"),
    VAL_AMOUNT_MAX_EXCEEDED("VAL-007", "El monto solicitado es mayor al máximo permitido."),
    VAL_STATUS_INVALID("VAL-008", "El estado '%s' no es válido o no está permitido para esta operación."),
    VAL_PAGINATION_INVALID("VAL-009", "Los parámetros de página y tamaño deben ser positivos."),
    VAL_INSUFFICIENT_INCOME("VAL-010", "Los ingresos reportados son insuficientes."),
    VAL_EXCESSIVE_DEBT_RATIO("VAL-011", "El nivel de endeudamiento actual es muy alto."),
    VAL_REQUIRES_MANUAL_REVIEW("VAL-012", "El monto supera el límite automático y requiere revisión manual."),
    VAL_INSUFFICIENT_DEBT_CAPACITY("VAL-013", "La capacidad de endeudamiento no es suficiente para cubrir la nueva cuota del préstamo."),
    VAL_FINANCIAL_DATA_INVALID("VAL-014", "Los datos financieros proporcionados son inconsistentes o inválidos."),

    // --- ERRORES DE AUTENTICACIÓN/AUTORIZACIÓN (401/403) ---
    AUTH_UNAUTHORIZED("AUTH-401", "Se requiere autenticación para realizar esta operación."),
    AUTH_FORBIDDEN("AUTH-403", "No tiene los permisos necesarios para acceder a este recurso."),

    // --- ERRORES DE NO ENCONTRADO (404 Not Found) ---
    APP_NOT_FOUND("APP-404", "La solicitud de préstamo con ID '%s' no fue encontrada."),
    LTY_NOT_FOUND("LTY-404", "El tipo de préstamo con ID '%s' no existe."),
    USER_DATA_NOT_FOUND("USR-404", "No se encontraron los datos financieros para el usuario con ID '%s'."),

    // --- ERRORES DE CONFLICTO (409 Conflict) ---
    APP_STATE_CONFLICT("APP-409", "La operación no puede realizarse porque el estado de la solicitud ha cambiado anteriormente. Estado actual: %s."),

    // --- ERRORES DE SERVIDOR (500 Internal Server Error) ---
    INTERNAL_SERVER_ERROR("SYS-500", "Ocurrió un error inesperado en el sistema."),

    SYS_CALCULATION_ERROR("SYS-001", "Error en los cálculos financieros."),
    SYS_PLAN_GENERATION_ERROR("SYS-002", "Error generando el plan de pagos.");

    private final String code;
    private final String message;



}
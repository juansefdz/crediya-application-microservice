package co.com.pragma.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LoanApplicationStatus {
    PENDIENTE_REVISION,
    APROBADA,
    RECHAZADA,
    REVISION_MANUAL,
    PENDIENTE_VALIDACION;
}
package co.com.pragma.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LoanApplicationStatus {
    PENDIENTE_REVISION(1),
    APROBADA(2),
    RECHAZADA(3),
    REVISION_MANUAL(4);

    private final int id;
    public int getId() { return id; }

    public static LoanApplicationStatus fromId(int id) {
        for (var s : values()) if (s.id == id) return s;
        throw new IllegalArgumentException("ID de estado inválido: " + id);
    }
}
package pe.unmsm.crm.marketing.leads.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoLead {
    NUEVO(1),
    CALIFICADO(2),
    DESCARTADO(3);

    private final int dbId;

    public static EstadoLead fromId(Integer id) {
        if (id == null)
            return null;
        for (EstadoLead e : values()) {
            if (e.dbId == id)
                return e;
        }
        throw new IllegalArgumentException("ID desconocido para EstadoLead: " + id);
    }
}
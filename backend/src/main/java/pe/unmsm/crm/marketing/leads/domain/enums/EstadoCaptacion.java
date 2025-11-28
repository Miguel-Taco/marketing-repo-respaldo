package pe.unmsm.crm.marketing.leads.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoCaptacion {
    EN_PROCESO(1),
    VALIDADO(2),
    RECHAZADO(3),
    DUPLICADO(4);

    private final int dbId;

    public static EstadoCaptacion fromId(Integer id) {
        if (id == null)
            return null;
        for (EstadoCaptacion e : values()) {
            if (e.dbId == id)
                return e;
        }
        throw new IllegalArgumentException("ID desconocido para EstadoCaptacion: " + id);
    }
}
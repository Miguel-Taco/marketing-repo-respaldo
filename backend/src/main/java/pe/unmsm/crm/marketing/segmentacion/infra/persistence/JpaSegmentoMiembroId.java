package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import lombok.Data;
import java.io.Serializable;

@Data
public class JpaSegmentoMiembroId implements Serializable {
    private Long idSegmento;
    private String tipoMiembro;
    private Long idMiembro;
}

package pe.unmsm.crm.marketing.segmentacion.domain.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Segmento {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipoAudiencia; // "LEAD" or "CLIENTE"
    private String estado; // "BORRADOR", "ACTIVO", etc.
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private ReglaSegmento reglaPrincipal;

    public Segmento() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estado = "INACTIVO";
    }

    public void actualizarFecha() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}

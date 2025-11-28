package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuionDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String objetivo;
    private String tipo; // VENTA, ENCUESTA, RETENCION
    private String estado; // BORRADOR, PUBLICADO, ARCHIVADO
    private List<PasoGuionDTO> pasos;
}

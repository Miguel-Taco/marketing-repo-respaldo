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
    private String objetivo;
    private String tipo; // VENTA, ENCUESTA, RETENCION, RENOVACION, VENTA_NUEVA, RECUPERO
    private String notasInternas;
    private String estado; // BORRADOR, PUBLICADO, ARCHIVADO
    private List<SeccionGuionDTO> pasos; // Renamed from 'secciones' to match frontend
}

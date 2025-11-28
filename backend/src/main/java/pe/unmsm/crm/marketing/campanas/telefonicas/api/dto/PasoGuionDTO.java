package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasoGuionDTO {
    private Integer orden;
    private String tipo; // INFORMATIVO, PREGUNTA_ABIERTA, PREGUNTA_CERRADA, OPCION_UNICA
    private String titulo;
    private String contenido;
    private String campoGuardado; // Campo donde se guarda la respuesta (si aplica)
}

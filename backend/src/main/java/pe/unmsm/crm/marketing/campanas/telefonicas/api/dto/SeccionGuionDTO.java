package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionGuionDTO {
    private Integer id;
    private String tipoSeccion; // INTRO, DIAGNOSTICO, OBJECIONES, CIERRE, POST_LLAMADA
    private String contenido;
    private Integer orden;
}

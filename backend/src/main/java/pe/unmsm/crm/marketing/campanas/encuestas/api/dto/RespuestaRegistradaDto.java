package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaRegistradaDto {

    private Integer idRespuestaEncuesta;
    private String mensaje;
    private LocalDateTime fechaRespuesta;
}

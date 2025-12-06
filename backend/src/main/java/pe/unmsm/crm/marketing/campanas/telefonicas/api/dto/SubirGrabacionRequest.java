package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubirGrabacionRequest {
    private Integer idCampania;
    private Long idLead;
    private Integer idLlamada;
    private Integer duracionSegundos;
    private String resultado;
    private MultipartFile archivo;
}

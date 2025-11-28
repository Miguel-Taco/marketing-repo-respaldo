package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactoDTO {
    private Long id;
    private Long idLead;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String empresa;
    private String estadoCampania; // NO_CONTACTADO, EN_SEGUIMIENTO, CERRADO, NO_INTERESADO
    private String prioridad; // ALTA, MEDIA, BAJA
    private String segmento;
    private Integer numeroIntentos;
    private LocalDateTime fechaUltimaLlamada;
    private String resultadoUltimaLlamada;
    private String notas;
}

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
public class LlamadaDTO {
    private Long id;
    private Long idCampania;
    private Long idContacto;
    private Long idAgente;
    private LocalDateTime fechaHora;
    private Integer duracionSegundos;
    private String resultado; // CONTACTADO, NO_CONTESTA, BUZON, NO_INTERESADO, INTERESADO, VENTA
    private String motivo;
    private String notas;
    private LocalDateTime fechaReagendamiento;
    private Boolean derivadoVentas;
    private String tipoOportunidad;
    
    // Info adicional del contacto para mostrar en historial
    private String nombreContacto;
    private String telefonoContacto;
}

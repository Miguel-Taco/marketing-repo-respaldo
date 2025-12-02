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
public class EnvioEncuestaDTO {
    private Integer id;
    private Integer idLlamada;
    private Integer idEncuesta;
    private Long idLead;

    // Datos de envío
    private String telefonoDestino;
    private String urlEncuesta;
    private LocalDateTime fechaEnvio;
    private String estado; // ENVIADA, ERROR, PENDIENTE
    private String metodoComunicacion; // SMS, WHATSAPP, EMAIL
    private String mensajeError;

    // Info adicional para el modal
    private String nombreLead;
    private String nombreCampania;
    private String tituloEncuesta;
}

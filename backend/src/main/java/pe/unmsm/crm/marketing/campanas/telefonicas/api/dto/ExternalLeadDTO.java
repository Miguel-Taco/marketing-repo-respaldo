package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para enviar datos de leads interesados al sistema externo de ventas.
 * 
 * Este objeto se serializa a JSON y se envía mediante POST al endpoint
 * /api/venta/lead/desde-marketing del sistema de ventas.
 */
@Data
@Builder
public class ExternalLeadDTO {

    @JsonProperty("idLeadMarketing")
    private Long idLeadMarketing;

    @JsonProperty("nombres")
    private String nombres;

    @JsonProperty("apellidos")
    private String apellidos;

    @JsonProperty("correo")
    private String correo;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("dni")
    private String dni;

    @JsonProperty("canalOrigen")
    private String canalOrigen;

    @JsonProperty("idCampaniaMarketing")
    private Long idCampaniaMarketing;

    @JsonProperty("nombreCampania")
    private String nombreCampania;

    @JsonProperty("tematica")
    private String tematica;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("notas_llamada")
    private String notasLlamada;

    @JsonProperty("fecha_envio")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaEnvio;
}

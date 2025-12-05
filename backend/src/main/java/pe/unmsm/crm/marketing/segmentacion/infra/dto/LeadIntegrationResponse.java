package pe.unmsm.crm.marketing.segmentacion.infra.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO para mapear la respuesta de la API de integración de Leads
 */
@Data
public class LeadIntegrationResponse {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String estado;
    private LocalDateTime fechaCreacion;
    private Integer edad;
    private String genero;
    private String distritoId;
    private String distritoNombre;
    private String provinciaNombre;
    private String departamentoNombre;
    private String nivelEducativo;
    private String estadoCivil;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String tipoFuente;
}

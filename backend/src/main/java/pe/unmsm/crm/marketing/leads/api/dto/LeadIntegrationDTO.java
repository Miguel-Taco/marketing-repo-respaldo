package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LeadIntegrationDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String estado;
    private LocalDateTime fechaCreacion;

    // Datos Demográficos (Segmentación)
    private Integer edad;
    private String genero;
    private String distritoId;
    private String distritoNombre; // Nombre del distrito
    private String provinciaNombre; // Nombre de la provincia
    private String departamentoNombre; // Nombre del departamento
    private String nivelEducativo;
    private String estadoCivil;

    // Tracking (Origen)
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String tipoFuente;
}

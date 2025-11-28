package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private Long id;
    private String nombreCompleto;
    private String estado;
    private String fechaCreacion;
    private DatosContactoDTO contacto;
    private DatosDemograficosDTO demograficos;
    private TrackingUTMDTO tracking;
    private String fuenteTipo;
    private java.util.List<HistorialEstadoLeadDTO> historial;
}

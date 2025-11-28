package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaTelefonicaDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado; // BORRADOR, ACTIVA, PAUSADA, FINALIZADA
    private String prioridad; // ALTA, MEDIA, BAJA
    private Long idGuion;
    private Integer totalLeads;
    private Integer leadsPendientes;
    private Integer leadsContactados;
    private Double porcentajeAvance;
    private List<Long> idsAgentes;
}

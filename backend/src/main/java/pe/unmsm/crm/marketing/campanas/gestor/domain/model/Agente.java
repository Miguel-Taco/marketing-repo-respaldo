package pe.unmsm.crm.marketing.campanas.gestor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa un Agente de Marketing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agente {
    private Integer idAgente;
    private String nombre;
    private String email;
    private String telefono;
    private Boolean activo;
}

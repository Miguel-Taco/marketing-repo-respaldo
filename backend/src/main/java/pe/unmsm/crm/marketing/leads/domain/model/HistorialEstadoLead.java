package pe.unmsm.crm.marketing.leads.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.EqualsAndHashCode;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.shared.domain.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estado_lead")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "historial_id")) // Mapea 'id' a 'historial_id'
public class HistorialEstadoLead extends BaseEntity {

    @Column(name = "lead_id", nullable = false)
    @NonNull
    private Long leadId;

    @Column(name = "estado_anterior_id")
    private EstadoLead estadoAnterior;

    @Column(name = "estado_nuevo_id", nullable = false)
    @NonNull
    private EstadoLead estadoNuevo;

    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;

    @Column(name = "motivo")
    private String motivo;
}

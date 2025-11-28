package pe.unmsm.crm.marketing.leads.domain.model.staging;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoCaptacion;

@Entity
@Table(name = "registros_importados")
@Data
@NoArgsConstructor
public class RegistroImportado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id")
    private Long id;

    @Column(name = "lote_id")
    private Long loteId;

    @Column(name = "estado_proceso_id", nullable = false)
    private EstadoCaptacion estadoProcesoId = EstadoCaptacion.EN_PROCESO;

    @Column(name = "datos_originales", columnDefinition = "json")
    private String datosJson;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @Transient
    private int numeroFila;
}
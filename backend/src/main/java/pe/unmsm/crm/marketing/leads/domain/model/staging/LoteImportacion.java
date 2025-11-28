package pe.unmsm.crm.marketing.leads.domain.model.staging;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import pe.unmsm.crm.marketing.shared.domain.BaseEntity;

@Entity
@Table(name = "lotes_importacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "lote_id"))
public class LoteImportacion extends BaseEntity {

    @Column(name = "origen")
    private String nombreArchivo;

    @Column(name = "total_registros")
    private Integer totalRegistros;

    @Column(name = "exitosos")
    private Integer exitosos = 0;

    @Column(name = "rechazados")
    private Integer rechazados = 0;

    @Column(name = "duplicados")
    private Integer duplicados = 0;

    @Column(name = "con_errores")
    private Integer conErrores = 0;

    // Estado del lote (simulado para el frontend)
    @Transient
    public String getEstadoCalculado() {
        if (totalRegistros == null || totalRegistros == 0)
            return "VACIO";
        if (exitosos + rechazados < totalRegistros)
            return "EN_PROCESO";
        if (rechazados > 0)
            return "CON_ERRORES";
        return "COMPLETADO";
    }
}

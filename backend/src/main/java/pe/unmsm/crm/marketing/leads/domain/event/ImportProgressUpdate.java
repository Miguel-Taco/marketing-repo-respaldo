package pe.unmsm.crm.marketing.leads.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para comunicar el progreso de importación en tiempo real via WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportProgressUpdate {

    private Long loteId;
    private String nombreArchivo;
    private int totalRegistros;
    private int procesados;
    private int exitosos;
    private int duplicados;
    private int conErrores;
    private boolean completado;

    /**
     * Calcula el porcentaje de progreso (0-100)
     */
    public double getPorcentajeProgreso() {
        if (totalRegistros == 0)
            return 0.0;
        return (procesados * 100.0) / totalRegistros;
    }

    /**
     * Total de registros rechazados (duplicados + con errores)
     */
    public int getRechazados() {
        return duplicados + conErrores;
    }
}

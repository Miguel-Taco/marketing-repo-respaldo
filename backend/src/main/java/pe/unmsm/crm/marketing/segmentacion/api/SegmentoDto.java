package pe.unmsm.crm.marketing.segmentacion.api;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SegmentoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipoAudiencia;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Integer cantidadMiembros; // Count of members without loading all

    // Simplified rule structure for DTO
    private ReglaDto reglaPrincipal;

    @Data
    public static class ReglaDto {
        private String tipo; // "SIMPLE", "AND", "OR"

        // For SIMPLE
        private Long idFiltro;
        private String campo;
        private String operador;
        private String valorTexto;
        // Add other value fields as needed

        // For AND/OR
        private List<ReglaDto> reglas;
    }
}

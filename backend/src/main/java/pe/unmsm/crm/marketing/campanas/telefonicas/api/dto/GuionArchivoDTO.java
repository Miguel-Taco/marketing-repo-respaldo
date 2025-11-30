package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un archivo de guión.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuionArchivoDTO {

    private Integer id;
    private Long idCampania;
    private Long idAgente;
    private String nombre;
    private String descripcion;
    private String tipoArchivo;
    private String fechaCreacion;
    private String fechaModificacion;
    private String urlDescarga;
    private String estado;
    private Boolean activo;
    private Boolean esGeneral;
}

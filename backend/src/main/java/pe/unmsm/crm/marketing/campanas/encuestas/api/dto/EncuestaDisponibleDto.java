package pe.unmsm.crm.marketing.campanas.encuestas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar encuestas disponibles (en estado ACTIVA).
 * Contiene solo la información mínima necesaria para listar encuestas
 * disponibles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncuestaDisponibleDto {

    private Integer idEncuesta;
    private String titulo;
}

package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO para la configuración de campaña telefónica.
 * Representa los ajustes de comportamiento de la campaña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaniaTelefonicaConfigDTO {

    private LocalTime horaInicioPermitida;
    private LocalTime horaFinPermitida;
    private String diasSemanaPermitidos;
    private Integer maxIntentos;
    private Integer intervaloReintentosMin;
    private String tipoDiscado; // Manual, Preview, Progresivo
    private String modoContacto; // Llamada, Llamada+SMS
    private Boolean permiteSmsRespaldo;
}

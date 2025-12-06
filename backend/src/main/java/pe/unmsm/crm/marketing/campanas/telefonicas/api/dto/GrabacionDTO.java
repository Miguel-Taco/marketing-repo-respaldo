package pe.unmsm.crm.marketing.campanas.telefonicas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrabacionDTO {
    private Long id;
    private Integer idCampania;
    private Integer idAgente;
    private Long idLead;
    private Integer idLlamada;
    private LocalDateTime fechaHora;
    private Integer duracionSegundos;
    private String rutaAudioFirebase;
    private String rutaTranscripcionSupabase;
    private String estadoProcesamiento; // PENDIENTE, PROCESANDO, COMPLETADO, ERROR
    private String resultado;
    private String mensajeError;
    private Integer intentosProcesamiento;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Información adicional para el listado
    private String nombreAgente;
    private String nombreLead;
    private String nombreCampania;
    private String telefonoLead;

    // URL pública temporal para reproducir el audio
    private String urlAudioPublica;

    // Contenido de la transcripción
    private String transcripcionCompleta;
}

package pe.unmsm.crm.marketing.campanas.mailing.domain.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.request.LeadVentasRequest;
import pe.unmsm.crm.marketing.campanas.mailing.api.dto.response.LeadInfoDTO;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.EventoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.TipoInteraccion;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.ILeadPort;
import pe.unmsm.crm.marketing.campanas.mailing.domain.port.output.IVentasPort;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaCampanaMailingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ✅ PATRÓN OBSERVER
 * 
 * Observer que deriva leads interesados a Ventas.
 * 
 * RESPONSABILIDAD:
 * - Solo reacciona a eventos de CLIC (interés activo)
 * - Obtiene información completa del lead
 * - Construye LeadVentasRequest
 * - Llama a VentasPort para derivar
 * 
 * VENTAJA DEL OBSERVER:
 * - Desacoplamiento total
 * - Se ejecuta de forma asíncrona
 * - No afecta el flujo principal si falla
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DerivarAVentasObserver {

    private final IVentasPort ventasPort;
    private final JpaCampanaMailingRepository campanaRepo;
    private final ILeadPort leadPort;

    @EventListener
    @Async
    public void onEventoInteraccion(EventoInteraccion evento) {
        try {
            // Solo derivar cuando hay CLIC
            if (evento.getTipoEvento() != TipoInteraccion.CLIC) {
                return;
            }
            
            log.info("Observer [VENTAS]: Derivando lead...");
            log.info("  Campaña: {}", evento.getIdCampanaMailingId());
            log.info("  Lead: {}", evento.getIdContactoCrm());
            
            // Obtener campaña
            CampanaMailing campana = campanaRepo
                    .findById(evento.getIdCampanaMailingId())
                    .orElse(null);
            
            if (campana == null) {
                log.warn("  ⚠ Campaña no encontrada");
                return;
            }
            
            // Obtener información completa del lead
            Optional<LeadInfoDTO> leadInfoOpt = leadPort
                    .findLeadInfoById(evento.getIdContactoCrm());
            
            if (leadInfoOpt.isEmpty()) {
                log.warn("  ⚠ Lead no encontrado");
                return;
            }
            
            LeadInfoDTO leadInfo = leadInfoOpt.get();
            
            // Construir request para Ventas
            LeadVentasRequest request = LeadVentasRequest.builder()
                    .idLeadMarketing(leadInfo.getLeadId())
                    .nombres(leadInfo.getNombresParaVentas())
                    .apellidos(leadInfo.getApellidosParaVentas())
                    .correo(evento.getEmailContacto())
                    .telefono(leadInfo.getTelefonoParaVentas())
                    .canalOrigen("CAMPANIA_MAILING")
                    .idCampaniaMarketing(campana.getIdCampanaGestion())
                    .nombreCampania(campana.getNombre())
                    .tematica(campana.getTematica())
                    .descripcion(campana.getDescripcion())
                    .notasLlamada(generarNotas(campana, leadInfo))
                    .fechaEnvio(LocalDateTime.now())
                    .build();
            
            // Derivar a Ventas
            boolean exito = ventasPort.derivarLeadInteresado(request);
            
            if (exito) {
                log.info("  ✓ Lead derivado exitosamente");
            } else {
                log.warn("  ⚠ No se pudo derivar");
            }
            
        } catch (Exception e) {
            log.error("Observer [VENTAS]: Error - {}", e.getMessage());
        }
    }

    private String generarNotas(CampanaMailing campana, LeadInfoDTO leadInfo) {
        return String.format(
            "Lead interesado desde campaña de mailing '%s'. " +
            "Cliente %s %s hizo clic en el CTA. " +
            "Temática: %s. Contacto prioritario.",
            campana.getNombre(),
            leadInfo.getNombresParaVentas(),
            leadInfo.getApellidosParaVentas(),
            campana.getTematica() != null ? campana.getTematica() : "General"
        );
    }
}
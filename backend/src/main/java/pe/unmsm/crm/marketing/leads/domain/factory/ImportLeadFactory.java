package pe.unmsm.crm.marketing.leads.domain.factory;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import pe.unmsm.crm.marketing.leads.domain.model.Lead;
import pe.unmsm.crm.marketing.leads.domain.model.LeadImportado;
import pe.unmsm.crm.marketing.leads.domain.model.staging.RegistroImportado;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.vo.DatosContacto;
import pe.unmsm.crm.marketing.leads.domain.vo.DatosDemograficos;
import pe.unmsm.crm.marketing.leads.domain.vo.TrackingUTM;
import pe.unmsm.crm.marketing.shared.application.service.UbigeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportLeadFactory implements LeadFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UbigeoService ubigeoService;

    @Override
    public Lead convertirALead(Object origenStaging) {
        if (!(origenStaging instanceof RegistroImportado)) {
            throw new IllegalArgumentException("El objeto origen no es un RegistroImportado");
        }
        RegistroImportado registro = (RegistroImportado) origenStaging;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> datos = objectMapper.readValue(registro.getDatosJson(), Map.class);

            LeadImportado lead = new LeadImportado();

            // CORRECCIÓN: Buscar nombre_completo (snake_case del Excel/Python)
            String nombre = String.valueOf(
                    datos.getOrDefault("nombre_completo", datos.getOrDefault("nombre", "Sin Nombre Importado")));
            lead.setNombre(nombre);

            lead.setIdReferenciaOrigen(registro.getId());
            lead.setRegistroImportadoId(registro.getId());
            // Leads importados se marcan como CALIFICADOS automáticamente
            lead.setEstado(EstadoLead.CALIFICADO);

            // LÓGICA DE UBIGEO MEJORADA
            // Acepta ID (150101) o Cadena ("Lima, Lima, Lima")
            // Busca en 'distrito_id' (estándar) o 'direccion' (script python usuario)
            String rawDistrito = (String) datos.get("distrito_id");
            if (rawDistrito == null) {
                rawDistrito = (String) datos.get("direccion");
            }

            log.debug("🏠 [IMPORT] Dato direccion/distrito_id: '{}'", rawDistrito);

            String distritoId = null;

            if (rawDistrito != null && !rawDistrito.trim().isEmpty()) {
                // 1. Intentar resolver como cadena "Distrito, Provincia, Departamento"
                distritoId = ubigeoService.buscarUbigeoPorCadena(rawDistrito);
                log.debug("🔎 [IMPORT] Resultado de buscarUbigeoPorCadena: '{}'", distritoId);

                // 2. Si devuelve null, verificar si era un ID directo
                if (distritoId == null && rawDistrito.matches("\\d{6}")) {
                    distritoId = rawDistrito;
                    log.debug("✅ [IMPORT] Usando ID directo: {}", distritoId);
                }
            } else {
                log.warn("⚠️ [IMPORT] No hay dato de dirección/distrito en el registro");
            }

            log.info("🎯 [IMPORT] DistritoId final asignado: '{}' para lead: {}", distritoId, nombre);

            // DatosContacto: solo email y telefono (distrito va en demograficos)
            DatosContacto contacto = new DatosContacto(
                    String.valueOf(datos.get("email")),
                    String.valueOf(datos.getOrDefault("telefono", "")));
            lead.setContacto(contacto);

            // Demograficos con distrito limpio
            if (datos.containsKey("edad") || datos.containsKey("genero") || datos.containsKey("distrito_id")
                    || datos.containsKey("direccion")) {
                Integer edad = parseIntSafe(datos.get("edad"));
                String genero = (String) datos.get("genero");
                lead.setDemograficos(new DatosDemograficos(edad, genero, distritoId));
            }

            TrackingUTM tracking = new TrackingUTM(
                    "IMPORTACION",
                    "ARCHIVO_EXCEL",
                    "LOTE_" + registro.getLoteId());
            lead.setTracking(tracking);

            return lead;

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar datos de importación: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean soporta(TipoFuente tipo) {
        return TipoFuente.IMPORTACION.equals(tipo);
    }

    private Integer parseIntSafe(Object value) {
        if (value == null)
            return null;
        try {
            // Si Excel devuelve 28.0 como Double
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            // Si viene como String
            return (int) Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
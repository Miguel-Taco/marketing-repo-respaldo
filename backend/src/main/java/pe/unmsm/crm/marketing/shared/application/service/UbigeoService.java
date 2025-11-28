package pe.unmsm.crm.marketing.shared.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.shared.domain.repository.DistritoRepository;

import org.springframework.cache.annotation.Cacheable;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UbigeoService {

    private final DistritoRepository distRepo;

    @Cacheable("ubigeo_nombres")
    public Map<String, String> obtenerNombresUbigeo(String distritoId) {
        if (distritoId == null || distritoId.length() != 6) {
            return new HashMap<>();
        }

        // OPTIMIZACIÓN: Una sola consulta para traer todo
        Map<String, String> result = distRepo.findNombresCompletos(distritoId);

        return result != null ? result : new HashMap<>();
    }

    public String buscarUbigeoPorCadena(String cadenaDireccion) {
        log.info("🔍 [UBIGEO] Buscando ubicación para: '{}'", cadenaDireccion);

        if (cadenaDireccion == null || cadenaDireccion.isEmpty()) {
            log.warn("⚠️ [UBIGEO] Cadena de dirección vacía o nula");
            return null;
        }

        // Separar por comas (ej: "Miraflores, Lima, Lima")
        String[] partes = cadenaDireccion.split(",");
        log.debug("📍 [UBIGEO] Partes separadas: {} parte(s)", partes.length);

        // Si no tiene 3 partes, intentamos buscar solo por distrito si es único
        // (opcional, pero mejor ser estricto por ahora)
        // O si el usuario envía solo el ID, lo validamos
        if (partes.length == 1) {
            String posibleId = partes[0].trim();
            log.debug("🔢 [UBIGEO] Verificando si '{}' es un ID de 6 dígitos", posibleId);
            if (posibleId.matches("\\d{6}") && distRepo.existsById(posibleId)) {
                log.info("✅ [UBIGEO] ID válido encontrado: {}", posibleId);
                return posibleId;
            }
            log.warn("❌ [UBIGEO] No es un ID válido de 6 dígitos o no existe en BD");
            return null; // No es un ID válido y no tiene formato de dirección completa
        }

        if (partes.length < 3) {
            log.warn("❌ [UBIGEO] Formato incompleto: se requieren 3 partes (Distrito, Provincia, Departamento)");
            return null; // Formato incompleto
        }

        String distritoNombre = partes[0].trim();
        String provinciaNombre = partes[1].trim();
        String departamentoNombre = partes[2].trim();

        log.info("📋 [UBIGEO] Buscando: Distrito='{}', Provincia='{}', Departamento='{}'",
                distritoNombre, provinciaNombre, departamentoNombre);

        // 1. OPTIMIZACIÓN: Usar una sola consulta con JOINs
        return distRepo.findIdByNombres(distritoNombre, provinciaNombre, departamentoNombre)
                .map(id -> {
                    log.info("✅ [UBIGEO] Ubicación encontrada: {} -> ID: {}", cadenaDireccion, id);
                    return id;
                })
                .orElseGet(() -> {
                    log.warn("❌ [UBIGEO] No se encontró ubicación exacta para: '{}'", cadenaDireccion);
                    return null;
                });
    }
}

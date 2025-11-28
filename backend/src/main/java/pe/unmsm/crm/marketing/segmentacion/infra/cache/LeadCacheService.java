package pe.unmsm.crm.marketing.segmentacion.infra.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.unmsm.crm.marketing.segmentacion.domain.model.*;
import pe.unmsm.crm.marketing.segmentacion.infra.dto.LeadIntegrationResponse;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeadCacheService {

    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<Long, LeadIntegrationResponse> leadCache;
    private volatile boolean cacheLoaded = false;
    private static final String LEAD_API_URL = "http://localhost:8080/api/v1/internal/leads/all";

    public LeadCacheService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.leadCache = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void warmUpCache() {
        log.info("=== Iniciando precarga de caché de leads ===");
        loadAllLeadsIntoCache();
    }

    private synchronized void loadAllLeadsIntoCache() {
        try {
            long startTime = System.currentTimeMillis();

            ResponseEntity<List<LeadIntegrationResponse>> response = restTemplate.exchange(
                    LEAD_API_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<LeadIntegrationResponse>>() {
                    });

            if (response.getBody() != null) {
                List<LeadIntegrationResponse> leads = response.getBody();
                leadCache.clear();
                leads.forEach(lead -> leadCache.put(lead.getId(), lead));

                cacheLoaded = true;
                long duration = System.currentTimeMillis() - startTime;

                log.info("✓ Caché de leads cargado exitosamente");
                log.info("  - Total leads: {}", leads.size());
                log.info("  - Tiempo de carga: {}ms", duration);
                log.info("Ejemplos de distritos en caché:");
                leads.stream().limit(5)
                        .forEach(lead -> log.info("  - Lead ID {}: distrito='{}'", lead.getId(), lead.getDistritoId()));
            } else {
                log.warn("⚠ API devolvió respuesta vacía");
                cacheLoaded = false;
            }
        } catch (Exception e) {
            log.error("✗ Error al cargar caché de leads: {}", e.getMessage());
            cacheLoaded = false;
        }
    }

    public List<LeadIntegrationResponse> getAllLeads() {
        if (!cacheLoaded) {
            log.warn("Caché no cargado, recargando...");
            loadAllLeadsIntoCache();
        }
        return new ArrayList<>(leadCache.values());
    }

    public List<Long> filterLeadsBySegment(Segmento segmento) {
        log.info("Filtrando {} leads en memoria para segmento '{}'", leadCache.size(), segmento.getNombre());

        // LOG DETALLADO DE LAS REGLAS
        if (segmento.getReglaPrincipal() != null) {
            logReglaDetails(segmento.getReglaPrincipal(), "");
        }

        long startTime = System.currentTimeMillis();

        List<Long> filteredIds = getAllLeads().stream()
                .filter(lead -> matchesSegmentRules(lead, segmento))
                .map(LeadIntegrationResponse::getId)
                .collect(Collectors.toList());

        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ Filtrado completado en {}ms - {} leads encontrados", duration, filteredIds.size());

        return filteredIds;
    }

    private void logReglaDetails(ReglaSegmento regla, String indent) {
        if (regla instanceof ReglaSimple) {
            ReglaSimple simple = (ReglaSimple) regla;
            log.info("{}REGLA SIMPLE: campo='{}', operador='{}', valor='{}'",
                    indent, simple.getCampo(), simple.getOperador(), simple.getValorTexto());
        } else if (regla instanceof GrupoReglasAnd) {
            log.info("{}GRUPO AND:", indent);
            ((GrupoReglasAnd) regla).getReglas().forEach(r -> logReglaDetails(r, indent + "  "));
        } else if (regla instanceof GrupoReglasOr) {
            log.info("{}GRUPO OR:", indent);
            ((GrupoReglasOr) regla).getReglas().forEach(r -> logReglaDetails(r, indent + "  "));
        }
    }

    public long countLeadsBySegment(Segmento segmento) {
        return getAllLeads().stream()
                .filter(lead -> matchesSegmentRules(lead, segmento))
                .count();
    }

    public void refreshCache() {
        log.info("Refrescando caché de leads manualmente...");
        loadAllLeadsIntoCache();
    }

    private boolean matchesSegmentRules(LeadIntegrationResponse lead, Segmento segmento) {
        if (segmento.getReglaPrincipal() == null) {
            return true;
        }
        return evaluateRule(lead, segmento.getReglaPrincipal());
    }

    private boolean evaluateRule(LeadIntegrationResponse lead, ReglaSegmento regla) {
        if (regla instanceof ReglaSimple) {
            return evaluateSimpleRule(lead, (ReglaSimple) regla);
        } else if (regla instanceof GrupoReglasAnd) {
            GrupoReglasAnd grupo = (GrupoReglasAnd) regla;
            return grupo.getReglas().stream()
                    .allMatch(r -> evaluateRule(lead, r));
        } else if (regla instanceof GrupoReglasOr) {
            GrupoReglasOr grupo = (GrupoReglasOr) regla;
            return grupo.getReglas().stream()
                    .anyMatch(r -> evaluateRule(lead, r));
        }
        return true;
    }

    private boolean evaluateSimpleRule(LeadIntegrationResponse lead, ReglaSimple regla) {
        String campo = regla.getCampo();
        String operador = regla.getOperador();
        String valor = regla.getValorTexto();

        if (campo == null || valor == null) {
            return true;
        }

        switch (campo.toLowerCase()) {
            case "edad":
                return evaluateEdad(lead.getEdad(), operador, valor);
            case "genero":
            case "género":
                return evaluateGenero(lead.getGenero(), operador, valor);
            case "distrito":
            case "distritoid":
            case "ciudad":
                return evaluateDistrito(lead.getDistritoId(), operador, valor);
            case "niveleducativo":
                return evaluateEquals(lead.getNivelEducativo(), valor, operador);
            case "estadocivil":
                return evaluateEquals(lead.getEstadoCivil(), valor, operador);
            default:
                log.warn("Campo no soportado para filtrado: {}", campo);
                return true;
        }
    }

    private boolean evaluateEdad(Integer edad, String operador, String valorStr) {
        if (edad == null)
            return false;

        try {
            int valorEdad = Integer.parseInt(valorStr.trim());

            switch (operador.toUpperCase()) {
                case "IGUAL":
                    return edad.equals(valorEdad);
                case "DIFERENTE":
                    return !edad.equals(valorEdad);
                case "MAYOR_QUE":
                    return edad > valorEdad;
                case "MENOR_QUE":
                    return edad < valorEdad;
                case "MAYOR_IGUAL":
                    return edad >= valorEdad;
                case "MENOR_IGUAL":
                    return edad <= valorEdad;
                default:
                    return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean evaluateGenero(String genero, String operador, String valor) {
        if (genero == null)
            return false;

        String generoNorm = normalizeGenero(genero);
        String valorNorm = normalizeGenero(valor);

        if ("IGUAL".equalsIgnoreCase(operador)) {
            return generoNorm.equals(valorNorm);
        } else if ("DIFERENTE".equalsIgnoreCase(operador)) {
            return !generoNorm.equals(valorNorm);
        }

        return true;
    }

    private String normalizeGenero(String genero) {
        if (genero == null)
            return "";
        String g = genero.toUpperCase().trim();
        if (g.equals("M") || g.equals("MASCULINO"))
            return "M";
        if (g.equals("F") || g.equals("FEMENINO"))
            return "F";
        return g;
    }

    private boolean evaluateDistrito(String distritoId, String operador, String valor) {
        if (distritoId == null) {
            return false;
        }

        // SOLUCIÓN TEMPORAL: Como el frontend envía nombres ('Lima') pero tenemos
        // códigos ('150101'),
        // vamos a hacer que CUALQUIER lead con distrito coincida si el usuario busca
        // por nombre común

        // Normalizar
        String distritoNorm = distritoId.trim().toLowerCase();
        String valorNorm = valor.trim().toLowerCase();

        // Si el valor es "lima", aceptar CUALQUIER código que empiece con "1501" (Lima)
        // Esto es una solución temporal hasta que la API devuelva nombres
        if ("lima".equals(valorNorm)) {
            // Códigos de Lima empiezan con 1501
            if ("IGUAL".equalsIgnoreCase(operador)) {
                return distritoNorm.startsWith("1501");
            }
        }

        // Comparación normal (código exacto)
        boolean result = false;
        if ("IGUAL".equalsIgnoreCase(operador)) {
            result = distritoNorm.equals(valorNorm);
        } else if ("DIFERENTE".equalsIgnoreCase(operador)) {
            result = !distritoNorm.equals(valorNorm);
        } else if ("CONTIENE".equalsIgnoreCase(operador)) {
            result = distritoNorm.contains(valorNorm);
        }

        return result;
    }

    private boolean evaluateEquals(String fieldValue, String expectedValue, String operador) {
        if (fieldValue == null)
            return false;

        if ("IGUAL".equalsIgnoreCase(operador)) {
            return fieldValue.equalsIgnoreCase(expectedValue.trim());
        } else if ("DIFERENTE".equalsIgnoreCase(operador)) {
            return !fieldValue.equalsIgnoreCase(expectedValue.trim());
        } else if ("CONTIENE".equalsIgnoreCase(operador)) {
            return fieldValue.toLowerCase().contains(expectedValue.toLowerCase().trim());
        }

        return true;
    }

    public CacheStats getStats() {
        return new CacheStats(leadCache.size(), cacheLoaded);
    }

    public static class CacheStats {
        public final int totalLeads;
        public final boolean loaded;

        public CacheStats(int totalLeads, boolean loaded) {
            this.totalLeads = totalLeads;
            this.loaded = loaded;
        }
    }
}

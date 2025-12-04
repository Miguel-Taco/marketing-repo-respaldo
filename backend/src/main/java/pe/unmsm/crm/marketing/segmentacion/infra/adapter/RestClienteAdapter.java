package pe.unmsm.crm.marketing.segmentacion.infra.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pe.unmsm.crm.marketing.segmentacion.application.ClienteServicePort;
import pe.unmsm.crm.marketing.segmentacion.application.dto.MarketingClienteDTO;
import pe.unmsm.crm.marketing.segmentacion.application.dto.PageMarketingClienteResponse;
import pe.unmsm.crm.marketing.segmentacion.domain.model.*;
import pe.unmsm.crm.marketing.segmentacion.domain.visitor.ReglaVisitor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador para consumir API externa de clientes
 * Filtra clientes según las reglas del segmento
 */
@Component
@RequiredArgsConstructor
public class RestClienteAdapter implements ClienteServicePort {

    private final RestTemplate restTemplate;

    @Value("${app.clientes.api.url:http://localhost:8081}")
    private String clientesApiUrl;

    @Override
    public List<Long> findClientesBySegmento(Segmento segmento) {
        System.out.println("=== RestClienteAdapter: Filtrando clientes desde API externa ===");

        // Obtener todos los clientes paginados
        List<MarketingClienteDTO> allClientes = fetchAllClientes();
        System.out.println("Total clientes obtenidos: " + allClientes.size());

        // Filtrar en memoria según las reglas
        List<Long> filteredIds = filterClientesByRules(allClientes, segmento.getReglaPrincipal());

        System.out.println("✓ Filtrado completado: " + filteredIds.size() + " clientes encontrados");
        return filteredIds;
    }

    @Override
    public long countClientesBySegmento(Segmento segmento) {
        return findClientesBySegmento(segmento).size();
    }

    /**
     * Obtiene todos los clientes de la API externa (paginado)
     * TEMPORAL: Usando datos MOCK para testing sin API externa
     */
    private List<MarketingClienteDTO> fetchAllClientes() {
        // ========== MOCK DATA PARA TESTING ==========
        // TODO: Reemplazar con llamada real a API cuando esté disponible
        System.out.println("⚠️  USANDO DATOS MOCK - Reemplazar con API real");

        List<MarketingClienteDTO> mockClientes = new ArrayList<>();
        String[] generos = { "MASCULINO", "FEMENINO", "OTRO" };
        String[] niveles = { "SECUNDARIA", "TECNICO", "UNIVERSITARIO", "POSTGRADO" };
        String[] ocupaciones = { "Ingeniero", "Médico", "Profesor", "Empresario", "Estudiante" };

        for (int i = 1; i <= 50; i++) {
            MarketingClienteDTO cliente = new MarketingClienteDTO();
            cliente.setIdCliente((long) i);
            cliente.setEmail("cliente" + i + "@test.com");
            cliente.setEdad(20 + (i % 40)); // Edades entre 20 y 60
            cliente.setGenero(generos[i % 3]);
            cliente.setNivelEducativo(niveles[i % 4]);
            cliente.setOcupacion(ocupaciones[i % 5]);
            cliente.setTotalGastado((double) (500 + i * 50)); // Entre 550 y 3000
            cliente.setTotalTransacciones(5 + (i % 20)); // Entre 5 y 25
            cliente.setScoreFidelidad(600 + (i * 8)); // Entre 608 y 1000
            cliente.setAceptaPublicidad(i % 3 != 0); // ~66% aceptan
            cliente.setIdioma("Español");
            mockClientes.add(cliente);
        }

        return mockClientes;

        /*
         * CÓDIGO ORIGINAL PARA RESTAURAR CUANDO TENGAS LA API:
         * List<MarketingClienteDTO> allClientes = new ArrayList<>();
         * int page = 0;
         * int totalPages = 1;
         * 
         * while (page < totalPages) {
         * String url = UriComponentsBuilder.fromHttpUrl(clientesApiUrl +
         * "/api/v1/clientes/marketing")
         * .queryParam("page", page)
         * .queryParam("size", 100)
         * .toUriString();
         * 
         * try {
         * ResponseEntity<PageMarketingClienteResponse> response =
         * restTemplate.exchange(
         * url,
         * HttpMethod.GET,
         * null,
         * new ParameterizedTypeReference<PageMarketingClienteResponse>() {
         * });
         * 
         * PageMarketingClienteResponse pageResponse = response.getBody();
         * if (pageResponse != null && pageResponse.getContent() != null) {
         * allClientes.addAll(pageResponse.getContent());
         * totalPages = pageResponse.getTotalPages();
         * }
         * page++;
         * } catch (Exception e) {
         * System.err.println("Error fetching clientes page " + page + ": " +
         * e.getMessage());
         * break;
         * }
         * }
         * 
         * return allClientes;
         */
    }

    /**
     * Filtra clientes en memoria según las reglas
     */
    private List<Long> filterClientesByRules(List<MarketingClienteDTO> clientes, ReglaSegmento regla) {
        if (regla == null) {
            return clientes.stream().map(MarketingClienteDTO::getIdCliente).collect(Collectors.toList());
        }

        return clientes.stream()
                .filter(cliente -> evaluateRule(cliente, regla))
                .map(MarketingClienteDTO::getIdCliente)
                .collect(Collectors.toList());
    }

    /**
     * Evalúa si un cliente cumple con una regla
     */
    private boolean evaluateRule(MarketingClienteDTO cliente, ReglaSegmento regla) {
        if (regla instanceof ReglaSimple) {
            return evaluateSimpleRule(cliente, (ReglaSimple) regla);
        } else if (regla instanceof GrupoReglasAnd) {
            GrupoReglasAnd grupo = (GrupoReglasAnd) regla;
            return grupo.getReglas().stream().allMatch(r -> evaluateRule(cliente, r));
        } else if (regla instanceof GrupoReglasOr) {
            GrupoReglasOr grupo = (GrupoReglasOr) regla;
            return grupo.getReglas().stream().anyMatch(r -> evaluateRule(cliente, r));
        }
        return false;
    }

    /**
     * Evalúa una regla simple contra un cliente
     */
    private boolean evaluateSimpleRule(MarketingClienteDTO cliente, ReglaSimple regla) {
        String campo = regla.getCampo();
        String operador = regla.getOperador();

        switch (campo) {
            case "edad":
                return evaluateNumeric(cliente.getEdad(), operador, regla);
            case "genero":
                return evaluateText(cliente.getGenero(), operador, regla.getValorTexto());
            case "nivelEducativo":
                return evaluateText(cliente.getNivelEducativo(), operador, regla.getValorTexto());
            case "ocupacion":
                return evaluateText(cliente.getOcupacion(), operador, regla.getValorTexto());
            case "total_gastado":
                return evaluateNumeric(cliente.getTotalGastado() != null ? cliente.getTotalGastado().intValue() : 0,
                        operador, regla);
            case "total_transacciones":
                return evaluateNumeric(cliente.getTotalTransacciones(), operador, regla);
            case "score_fidelidad":
                return evaluateNumeric(cliente.getScoreFidelidad(), operador, regla);
            case "acepta_publicidad":
                return evaluateBoolean(cliente.getAceptaPublicidad(), operador, regla.getValorTexto());
            default:
                return true;
        }
    }

    private boolean evaluateNumeric(Number value, String operador, ReglaSimple regla) {
        if (value == null)
            return false;
        int val = value.intValue();

        switch (operador) {
            case "IGUAL":
                return regla.getValorNumeroDesde() != null && val == regla.getValorNumeroDesde().intValue();
            case "MAYOR_QUE":
                return regla.getValorNumeroDesde() != null && val > regla.getValorNumeroDesde().intValue();
            case "MENOR_QUE":
                return regla.getValorNumeroDesde() != null && val < regla.getValorNumeroDesde().intValue();
            case "ENTRE":
                return regla.getValorNumeroDesde() != null && regla.getValorNumeroHasta() != null &&
                        val >= regla.getValorNumeroDesde().intValue() && val <= regla.getValorNumeroHasta().intValue();
            default:
                return false;
        }
    }

    private boolean evaluateText(String value, String operador, String expected) {
        if (value == null)
            return false;

        switch (operador) {
            case "IGUAL":
                return value.equalsIgnoreCase(expected);
            case "DISTINTO":
                return !value.equalsIgnoreCase(expected);
            case "CONTIENE":
                return value.toLowerCase().contains(expected.toLowerCase());
            default:
                return false;
        }
    }

    private boolean evaluateBoolean(Boolean value, String operador, String expected) {
        if (value == null)
            return false;
        boolean expectedBool = "true".equalsIgnoreCase(expected) || "1".equals(expected);
        return value == expectedBool;
    }
}

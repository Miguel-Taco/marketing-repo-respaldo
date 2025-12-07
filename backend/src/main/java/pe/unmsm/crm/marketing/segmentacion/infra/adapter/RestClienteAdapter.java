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

    @Value("${app.clientes.api.url:https://mod-ventas.onrender.com}")
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

    @Override
    public List<MarketingClienteDTO> getAllClientes() {
        return fetchAllClientes();
    }

    private List<MarketingClienteDTO> fetchAllClientes() {
        System.out.println("==============================================");
        System.out.println("🔄 [CLIENTES] Iniciando carga desde API de Ventas");
        System.out.println("🔄 [CLIENTES] URL Base: " + clientesApiUrl);
        System.out.println("==============================================");

        List<MarketingClienteDTO> allClientes = new ArrayList<>();
        int page = 0;
        int totalPages = 1;

        while (page < totalPages) {
            String url = UriComponentsBuilder.fromHttpUrl(clientesApiUrl + "/api/clientes/integracion/marketing")
                    .queryParam("page", page)
                    .queryParam("size", 100)
                    .toUriString();

            try {
                System.out.println("📡 [CLIENTES] Llamando a: " + url);
                ResponseEntity<PageMarketingClienteResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PageMarketingClienteResponse>() {
                        });

                PageMarketingClienteResponse pageResponse = response.getBody();
                if (pageResponse != null && pageResponse.getClientes() != null) {
                    allClientes.addAll(pageResponse.getClientes());
                    totalPages = pageResponse.getTotalPages();
                    System.out.println("  ✓ Página " + (page + 1) + "/" + totalPages + ": "
                            + pageResponse.getClientes().size() + " clientes");
                }
                page++;
            } catch (Exception e) {
                System.err.println("✗ [CLIENTES] Error en página " + (page + 1) + ": " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }

        System.out.println("✓ [CLIENTES] Total cargados: " + allClientes.size());
        System.out.println("==============================================");
        return allClientes;
    }

    /**
     * Filtra clientes en memoria según las reglas
     */
    private List<Long> filterClientesByRules(List<MarketingClienteDTO> clientes, ReglaSegmento regla) {
        if (regla == null) {
            return clientes.stream().map(MarketingClienteDTO::getClienteId).collect(Collectors.toList());
        }

        return clientes.stream()
                .filter(cliente -> evaluateRule(cliente, regla))
                .map(MarketingClienteDTO::getClienteId)
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
            case "email":
                return evaluateText(cliente.getEmail(), operador, regla.getValorTexto());
            case "nombre":
            case "fullName":
                return evaluateText(cliente.getFullName(), operador, regla.getValorTexto());
            case "dni":
                return evaluateText(cliente.getDni(), operador, regla.getValorTexto());
            case "categoria":
                return evaluateText(cliente.getCategoria(), operador, regla.getValorTexto());
            case "estado":
                return evaluateText(cliente.getEstado(), operador, regla.getValorTexto());
            case "ubicacion":
                return evaluateText(cliente.getUbicacion(), operador, regla.getValorTexto());
            case "total_gastado":
            case "monetaryScore":
                return evaluateNumeric(cliente.getMonetaryScore(), operador, regla);
            case "total_transacciones":
            case "frequencyScore":
                return evaluateNumeric(cliente.getFrequencyScore(), operador, regla);
            case "score":
            case "recencyScore":
                return evaluateNumeric(cliente.getRecencyScore(), operador, regla);
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

}

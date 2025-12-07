package pe.unmsm.crm.marketing.segmentacion.application.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO para respuesta paginada de clientes desde API externa de Ventas
 * Mapea: GET https://mod-ventas.onrender.com/api/clientes/integracion/marketing
 */
@Data
public class PageMarketingClienteResponse {
    private List<MarketingClienteDTO> clientes; // Cambio: "content" -> "clientes"
    private Integer currentPage; // Cambio: "number" -> "currentPage"
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize; // Cambio: "size" -> "pageSize"
}

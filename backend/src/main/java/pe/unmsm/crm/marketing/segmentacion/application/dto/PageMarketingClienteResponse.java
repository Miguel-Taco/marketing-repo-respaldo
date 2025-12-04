package pe.unmsm.crm.marketing.segmentacion.application.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO para respuesta paginada de clientes desde API externa
 */
@Data
public class PageMarketingClienteResponse {
    private List<MarketingClienteDTO> content;
    private Integer totalPages;
    private Long totalElements;
    private Integer size;
    private Integer number;
}

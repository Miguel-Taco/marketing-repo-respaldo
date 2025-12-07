package pe.unmsm.crm.marketing.segmentacion.application.dto;

import lombok.Data;

/**
 * DTO para datos de cliente desde API externa de Ventas
 * Mapea la respuesta de: GET
 * https://mod-ventas.onrender.com/api/clientes/integracion/marketing
 */
@Data
public class MarketingClienteDTO {
    private Long clienteId;
    private String dni;
    private String fullName;
    private String email;
    private String categoria; // Platino, Oro, Plata, etc.
    private String estado; // ACTIVO, INACTIVO
    private Integer recencyScore; // RFM: Recencia
    private Integer frequencyScore; // RFM: Frecuencia
    private Integer monetaryScore; // RFM: Valor monetario
    private String ubicacion; // Ciudad/Departamento
    private Integer edad;
}

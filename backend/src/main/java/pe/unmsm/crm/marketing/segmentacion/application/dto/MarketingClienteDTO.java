package pe.unmsm.crm.marketing.segmentacion.application.dto;

import lombok.Data;

/**
 * DTO para datos de cliente desde API externa de marketing
 */
@Data
public class MarketingClienteDTO {
    private Long idCliente;
    private String email;
    private Integer edad;
    private String genero; // MASCULINO, FEMENINO, OTRO
    private String nivelEducativo; // SECUNDARIA, TECNICO, UNIVERSITARIO, POSTGRADO
    private String ocupacion;
    private String address;
    private Double totalGastado;
    private Integer totalTransacciones;
    private String interesesDeclarados;
    private String canalContactoFavorito;
    private Boolean aceptaPublicidad;
    private Integer scoreFidelidad;
    private String idioma;
}

package pe.unmsm.crm.marketing.campanas.mailing.api.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarContenidoRequest {
    private String asunto;
    private String cuerpo;
    private String ctaTexto;
}
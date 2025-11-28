package pe.unmsm.crm.marketing.campanas.mailing.api.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricasMailingResponse {
    private Integer id;
    private Integer idCampanaMailingId;
    private Integer enviados;
    private Integer entregados;
    private Integer aperturas;
    private Integer clics;
    private Integer rebotes;
    private Integer bajas;
    private Double tasaApertura; // (aperturas / enviados) * 100
    private Double tasaClics;    // (clics / enviados) * 100
    private Double tasaBajas;    // (bajas / enviados) * 100
}
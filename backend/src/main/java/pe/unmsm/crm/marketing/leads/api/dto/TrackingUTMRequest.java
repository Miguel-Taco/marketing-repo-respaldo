package pe.unmsm.crm.marketing.leads.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUTMRequest {

    private String source;
    private String medium;
    private String campaign;
    private String term;
    private String content;
}

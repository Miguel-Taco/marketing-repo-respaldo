package pe.unmsm.crm.marketing.leads.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUTM {
    String source;
    String medium;
    String campaign;
    String term;
    String content;

    // Constructor de conveniencia (3 parámetros)
    public TrackingUTM(String source, String medium, String campaign) {
        this.source = source;
        this.medium = medium;
        this.campaign = campaign;
        this.term = null;
        this.content = null;
    }
}
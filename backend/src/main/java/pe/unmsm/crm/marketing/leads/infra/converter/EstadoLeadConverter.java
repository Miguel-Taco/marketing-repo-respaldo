package pe.unmsm.crm.marketing.leads.infra.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;

@Converter(autoApply = true) // Se aplicará automáticamente a cualquier campo EstadoLead
public class EstadoLeadConverter implements AttributeConverter<EstadoLead, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EstadoLead attribute) {
        return (attribute == null) ? null : attribute.getDbId();
    }

    @Override
    public EstadoLead convertToEntityAttribute(Integer dbData) {
        return EstadoLead.fromId(dbData);
    }
}

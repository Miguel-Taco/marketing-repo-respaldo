package pe.unmsm.crm.marketing.leads.infra.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoCaptacion;

@Converter(autoApply = true)
public class EstadoCaptacionConverter implements AttributeConverter<EstadoCaptacion, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EstadoCaptacion attribute) {
        return (attribute == null) ? null : attribute.getDbId();
    }

    @Override
    public EstadoCaptacion convertToEntityAttribute(Integer dbData) {
        return EstadoCaptacion.fromId(dbData);
    }
}

package pe.unmsm.crm.marketing.campanas.gestor.domain.state.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.*;

/**
 * Converter JPA para convertir entre el String de la base de datos
 * y la instancia concreta de EstadoCampana (Patrón State).
 */
@Converter(autoApply = false)
public class EstadoCampanaConverter implements AttributeConverter<EstadoCampana, String> {

    @Override
    public String convertToDatabaseColumn(EstadoCampana estado) {
        if (estado == null) {
            return null;
        }
        return estado.getNombre();
    }

    @Override
    public EstadoCampana convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return switch (dbData) {
            case "Borrador" -> new EstadoBorrador();
            case "Programada" -> new EstadoProgramada();
            case "Vigente" -> new EstadoVigente();
            case "Pausada" -> new EstadoPausada();
            case "Finalizada" -> new EstadoFinalizada();
            case "Cancelada" -> new EstadoCancelada();
            default -> throw new IllegalArgumentException("Estado desconocido: " + dbData);
        };
    }
}

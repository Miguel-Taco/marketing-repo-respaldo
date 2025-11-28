package pe.unmsm.crm.marketing.campanas.telefonicas.domain.strategy.assignment;

import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.CampaniaTelefonicaDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ContactoDTO;

import java.util.List;

/**
 * Estrategia para seleccionar/quien atiende un contacto.
 */
public interface CallAssignmentStrategy {

    /**
     * Selecciona el agente que atenderá el siguiente contacto.
     *
     * @param campania campaña a atender
     * @param cola contactos pendientes
     * @param agenteSolicitado agente indicado por el cliente (puede ser null)
     * @return id del agente que debe atender o null si no hay asignación posible
     */
    Long assign(CampaniaTelefonicaDTO campania, List<ContactoDTO> cola, Long agenteSolicitado);
}

package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Campana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.CanalEjecucion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Prioridad;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.CampanaRepositoryPort;
import pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoCampana;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository.JpaCampanaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto CampanaRepositoryPort
 * delegando a JpaCampanaRepository (Spring Data JPA).
 */
@Component
@RequiredArgsConstructor
public class CampanaRepositoryAdapter implements CampanaRepositoryPort {

    private final JpaCampanaRepository jpaRepository;

    @Override
    public Campana save(Campana campana) {
        return jpaRepository.save(campana);
    }

    @Override
    public Optional<Campana> findById(Long idCampana) {
        return jpaRepository.findById(idCampana);
    }

    @Override
    public List<Campana> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Campana> findByFiltros(String nombre, String estado, String prioridad, String canalEjecucion,
            Boolean esArchivado) {
        EstadoCampana estadoObj = null;
        if (estado != null && !estado.isEmpty()) {
            estadoObj = convertirEstado(estado);
        }

        Prioridad prioridadObj = null;
        if (prioridad != null && !prioridad.isEmpty()) {
            try {
                prioridadObj = Prioridad.valueOf(prioridad);
            } catch (IllegalArgumentException e) {
                // Ignorar prioridad inválida
            }
        }

        CanalEjecucion canalObj = null;
        if (canalEjecucion != null && !canalEjecucion.isEmpty()) {
            try {
                canalObj = CanalEjecucion.valueOf(canalEjecucion);
            } catch (IllegalArgumentException e) {
                // Si el canal no es válido, podríamos ignorarlo o lanzar error.
                // Aquí lo ignoramos (null) para que no filtre por canal inválido.
            }
        }

        return jpaRepository.findByFiltros(nombre, estadoObj, prioridadObj, canalObj, esArchivado);
    }

    private EstadoCampana convertirEstado(String nombreEstado) {
        return switch (nombreEstado) {
            case "Borrador" -> new pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoBorrador();
            case "Programada" -> new pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoProgramada();
            case "Vigente" -> new pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoVigente();
            case "Pausada" -> new pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoPausada();
            case "Finalizada" -> new pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoFinalizada();
            case "Cancelada" -> new pe.unmsm.crm.marketing.campanas.gestor.domain.state.EstadoCancelada();
            default -> null; // O lanzar excepción
        };
    }

    @Override
    public void deleteById(Long idCampana) {
        jpaRepository.deleteById(idCampana);
    }

    @Override
    public boolean existsById(Long idCampana) {
        return jpaRepository.existsById(idCampana);
    }
}

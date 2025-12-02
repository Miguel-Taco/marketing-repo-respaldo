package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.HistorialCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.TipoAccion;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.HistorialRepositoryPort;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository.JpaHistorialRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Adaptador que implementa el puerto HistorialRepositoryPort
 * delegando a JpaHistorialRepository (Spring Data JPA).
 */
@Component
@RequiredArgsConstructor
public class HistorialRepositoryAdapter implements HistorialRepositoryPort {

    private final JpaHistorialRepository jpaRepository;

    @Override
    public HistorialCampana save(HistorialCampana historial) {
        return jpaRepository.save(historial);
    }

    @Override
    public List<HistorialCampana> findByIdCampana(Long idCampana) {
        return jpaRepository.findByIdCampanaOrderByFechaAccionDesc(idCampana);
    }

    @Override
    public Page<HistorialCampana> findByFiltros(Long idCampana, String tipoAccion, LocalDateTime fechaDesde,
            LocalDateTime fechaHasta, Pageable pageable) {
        TipoAccion tipoAccionObj = null;
        if (tipoAccion != null && !tipoAccion.isEmpty()) {
            try {
                tipoAccionObj = TipoAccion.valueOf(tipoAccion);
            } catch (IllegalArgumentException e) {
                // Si el tipo de acción no es válido, lo ignoramos
            }
        }
        return jpaRepository.findByFiltros(idCampana, tipoAccionObj, fechaDesde, fechaHasta, pageable);
    }

    @Override
    public List<HistorialCampana> findAll() {
        return jpaRepository.findAll();
    }
}

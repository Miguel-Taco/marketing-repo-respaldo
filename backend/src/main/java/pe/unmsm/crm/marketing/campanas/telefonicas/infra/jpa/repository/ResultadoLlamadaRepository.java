package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.ResultadoLlamadaEntity;

import java.util.List;

@Repository
public interface ResultadoLlamadaRepository extends JpaRepository<ResultadoLlamadaEntity, Integer> {

    List<ResultadoLlamadaEntity> findAllByOrderByNombreAsc();

    List<ResultadoLlamadaEntity> findByActivoTrue();
}

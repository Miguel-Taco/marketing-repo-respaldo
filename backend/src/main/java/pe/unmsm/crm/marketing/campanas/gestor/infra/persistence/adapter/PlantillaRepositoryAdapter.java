package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.PlantillaCampana;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.PlantillaRepositoryPort;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository.JpaPlantillaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto PlantillaRepositoryPort
 * delegando a JpaPlantillaRepository (Spring Data JPA).
 */
@Component
@RequiredArgsConstructor
public class PlantillaRepositoryAdapter implements PlantillaRepositoryPort {

    private final JpaPlantillaRepository jpaRepository;

    @Override
    public PlantillaCampana save(PlantillaCampana plantilla) {
        return jpaRepository.save(plantilla);
    }

    @Override
    public Optional<PlantillaCampana> findById(Integer idPlantilla) {
        return jpaRepository.findById(idPlantilla);
    }

    @Override
    public List<PlantillaCampana> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<PlantillaCampana> findByFiltros(String nombre, String canalEjecucion) {
        return jpaRepository.findByFiltros(nombre, canalEjecucion);
    }

    @Override
    public void deleteById(Integer idPlantilla) {
        jpaRepository.deleteById(idPlantilla);
    }

    @Override
    public boolean existsById(Integer idPlantilla) {
        return jpaRepository.existsById(idPlantilla);
    }
}

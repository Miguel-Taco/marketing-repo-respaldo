package pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.campanas.gestor.domain.model.Agente;
import pe.unmsm.crm.marketing.campanas.gestor.domain.port.output.AgenteRepositoryPort;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.entity.AgenteEntity;
import pe.unmsm.crm.marketing.campanas.gestor.infra.persistence.repository.JpaAgenteRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AgenteRepositoryAdapter implements AgenteRepositoryPort {

    private final JpaAgenteRepository jpaRepository;

    @Override
    public List<Agente> findAllActive() {
        return jpaRepository.findByActivoTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Agente> findById(Integer id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existsAndActive(Integer id) {
        return jpaRepository.findById(id)
                .map(AgenteEntity::getActivo)
                .orElse(false);
    }

    private Agente toDomain(AgenteEntity entity) {
        return Agente.builder()
                .idAgente(entity.getIdAgente())
                .nombre(entity.getNombre())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .activo(entity.getActivo())
                .build();
    }
}

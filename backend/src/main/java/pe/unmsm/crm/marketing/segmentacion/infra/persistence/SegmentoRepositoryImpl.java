package pe.unmsm.crm.marketing.segmentacion.infra.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.unmsm.crm.marketing.segmentacion.domain.model.*;
import pe.unmsm.crm.marketing.segmentacion.domain.repository.SegmentoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SegmentoRepositoryImpl implements SegmentoRepository {

    private final JpaSegmentoRepository jpaSegmentoRepository;
    private final JpaSegmentoFiltroRepository jpaSegmentoFiltroRepository;
    private final JpaCatalogoFiltroRepository jpaCatalogoFiltroRepository;

    public SegmentoRepositoryImpl(JpaSegmentoRepository jpaSegmentoRepository,
            JpaSegmentoFiltroRepository jpaSegmentoFiltroRepository,
            JpaCatalogoFiltroRepository jpaCatalogoFiltroRepository) {
        this.jpaSegmentoRepository = jpaSegmentoRepository;
        this.jpaSegmentoFiltroRepository = jpaSegmentoFiltroRepository;
        this.jpaCatalogoFiltroRepository = jpaCatalogoFiltroRepository;
    }

    @Override
    @Transactional
    public Segmento save(Segmento segmento) {
        JpaSegmentoEntity entity = mapToEntity(segmento);
        entity = jpaSegmentoRepository.save(entity);

        // Handle rules persistence (Simplified: Delete all and re-insert)
        Long segmentoId = entity.getIdSegmento();
        if (segmentoId != null) {
            System.out.println("=== Eliminando filtros antiguos para segmento ID: " + segmentoId + " ===");
            jpaSegmentoFiltroRepository.deleteBySegmentoIdSegmento(segmentoId);
        }

        if (segmento.getReglaPrincipal() != null) {
            System.out.println("=== Guardando nuevos filtros para segmento ID: " + segmentoId + " ===");
            List<JpaSegmentoFiltroEntity> filtros = mapRulesToFiltros(segmento.getReglaPrincipal(), entity);
            if (!filtros.isEmpty()) {
                System.out.println("Insertando " + filtros.size() + " filtros");
                jpaSegmentoFiltroRepository.saveAll(filtros);
                System.out.println("✓ Filtros guardados exitosamente");
            } else {
                System.out.println("⚠ No hay filtros para guardar");
            }
        } else {
            System.out.println("⚠ reglaPrincipal es null, no se guardarán filtros");
        }

        return mapToDomain(entity);
    }

    @Override
    public Optional<Segmento> findById(Long id) {
        return jpaSegmentoRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<Segmento> findAll() {
        // Use optimized query with JOIN FETCH to prevent N+1 problem
        List<JpaSegmentoEntity> entities = jpaSegmentoRepository.findAllWithFiltersAndCatalog();
        return entities.stream()
                .map(this::mapToDomainOptimized)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaSegmentoRepository.deleteById(id);
    }

    private JpaSegmentoEntity mapToEntity(Segmento domain) {
        JpaSegmentoEntity entity = new JpaSegmentoEntity();
        entity.setIdSegmento(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setTipoAudiencia(domain.getTipoAudiencia());
        entity.setEstado(domain.getEstado());
        entity.setCantidadMiembros(domain.getCantidadMiembros());
        entity.setFechaCreacion(domain.getFechaCreacion());
        entity.setFechaActualizacion(domain.getFechaActualizacion());
        return entity;
    }

    private Segmento mapToDomain(JpaSegmentoEntity entity) {
        Segmento domain = new Segmento();
        domain.setId(entity.getIdSegmento());
        domain.setNombre(entity.getNombre());
        domain.setDescripcion(entity.getDescripcion());
        domain.setTipoAudiencia(entity.getTipoAudiencia());
        domain.setEstado(entity.getEstado());
        domain.setCantidadMiembros(entity.getCantidadMiembros());
        domain.setFechaCreacion(entity.getFechaCreacion());
        domain.setFechaActualizacion(entity.getFechaActualizacion());

        // Load rules
        List<JpaSegmentoFiltroEntity> filtros = jpaSegmentoFiltroRepository
                .findBySegmentoIdSegmento(entity.getIdSegmento());
        if (!filtros.isEmpty()) {
            GrupoReglasAnd root = new GrupoReglasAnd();
            for (JpaSegmentoFiltroEntity filtro : filtros) {
                ReglaSimple regla = new ReglaSimple();
                regla.setIdFiltro(filtro.getIdFiltro());

                // Resolve field name from ID
                jpaCatalogoFiltroRepository.findById(filtro.getIdFiltro())
                        .ifPresent(cat -> regla.setCampo(cat.getCampo()));

                regla.setOperador(filtro.getOperador());
                regla.setValorTexto(filtro.getValorTexto());
                root.addRegla(regla);
            }
            domain.setReglaPrincipal(root);
        }

        return domain;
    }

    /**
     * Optimized version that uses already loaded relationships (from JOIN FETCH)
     * Prevents N+1 query problem
     */
    private Segmento mapToDomainOptimized(JpaSegmentoEntity entity) {
        Segmento domain = new Segmento();
        domain.setId(entity.getIdSegmento());
        domain.setNombre(entity.getNombre());
        domain.setDescripcion(entity.getDescripcion());
        domain.setTipoAudiencia(entity.getTipoAudiencia());
        domain.setEstado(entity.getEstado());
        domain.setCantidadMiembros(entity.getCantidadMiembros());
        domain.setFechaCreacion(entity.getFechaCreacion());
        domain.setFechaActualizacion(entity.getFechaActualizacion());

        // Use already loaded filtros (no additional query)
        List<JpaSegmentoFiltroEntity> filtros = entity.getFiltros();
        if (filtros != null && !filtros.isEmpty()) {
            GrupoReglasAnd root = new GrupoReglasAnd();
            for (JpaSegmentoFiltroEntity filtro : filtros) {
                ReglaSimple regla = new ReglaSimple();
                regla.setIdFiltro(filtro.getIdFiltro());
                regla.setOperador(filtro.getOperador());
                regla.setValorTexto(filtro.getValorTexto());

                // Use already loaded catalogo (no additional query)
                if (filtro.getCatalogo() != null) {
                    regla.setCampo(filtro.getCatalogo().getCampo());
                }

                root.addRegla(regla);
            }
            domain.setReglaPrincipal(root);
        }

        return domain;
    }

    private List<JpaSegmentoFiltroEntity> mapRulesToFiltros(ReglaSegmento regla, JpaSegmentoEntity segmentoEntity) {
        List<JpaSegmentoFiltroEntity> result = new ArrayList<>();

        if (regla instanceof GrupoReglasAnd) {
            for (ReglaSegmento child : ((GrupoReglasAnd) regla).getReglas()) {
                result.addAll(mapRulesToFiltros(child, segmentoEntity));
            }
        } else if (regla instanceof ReglaSimple) {
            ReglaSimple simple = (ReglaSimple) regla;

            System.out.println("=== DEBUG: Procesando ReglaSimple ===");
            System.out.println("Campo recibido: '" + simple.getCampo() + "'");
            System.out.println("idFiltro existente: " + simple.getIdFiltro());
            System.out.println("valorTexto: " + simple.getValorTexto());

            // Resolve ID from field name
            Long idFiltro = simple.getIdFiltro();
            if (idFiltro == null && simple.getCampo() != null) {
                String campoNormalizado = simple.getCampo().toLowerCase().trim();
                System.out.println("Buscando en catálogo con campo normalizado: '" + campoNormalizado + "'");

                List<JpaCatalogoFiltroEntity> catalogos = jpaCatalogoFiltroRepository.findByCampo(campoNormalizado);
                if (!catalogos.isEmpty()) {
                    if (catalogos.size() > 1) {
                        System.out.println("⚠ ADVERTENCIA: Se encontraron " + catalogos.size()
                                + " filtros duplicados para '" + campoNormalizado + "'. Usando el primero.");
                    }
                    idFiltro = catalogos.get(0).getIdFiltro();
                    System.out.println("✓ Filtro encontrado! ID: " + idFiltro);
                } else {
                    System.err.println(
                            "✗ ERROR: Filtro NO encontrado en catálogo para campo: '" + campoNormalizado + "'");

                    // Mostrar todos los campos disponibles
                    List<JpaCatalogoFiltroEntity> todosFiltros = jpaCatalogoFiltroRepository.findAll();
                    System.err.println("Campos disponibles en catálogo:");
                    todosFiltros.forEach(
                            f -> System.err.println("  - '" + f.getCampo() + "' (ID: " + f.getIdFiltro() + ")"));

                    return result; // Skip this rule
                }
            }

            if (idFiltro != null) {
                JpaSegmentoFiltroEntity entity = new JpaSegmentoFiltroEntity();
                entity.setSegmento(segmentoEntity);
                entity.setIdFiltro(idFiltro);
                entity.setOperador(simple.getOperador());
                entity.setValorTexto(simple.getValorTexto());
                result.add(entity);
                System.out.println("✓ Filtro agregado correctamente");
            } else {
                System.err.println("✗ ERROR: idFiltro es null, no se puede agregar el filtro");
            }
        }

        return result;
    }
}

package pe.unmsm.crm.marketing.segmentacion.api;

import org.springframework.stereotype.Component;
import pe.unmsm.crm.marketing.segmentacion.domain.model.*;
import pe.unmsm.crm.marketing.segmentacion.infra.persistence.JpaSegmentoMiembroRepository;

import java.util.stream.Collectors;

@Component
public class SegmentoMapper {

    private final JpaSegmentoMiembroRepository miembroRepository;

    public SegmentoMapper(JpaSegmentoMiembroRepository miembroRepository) {
        this.miembroRepository = miembroRepository;
    }

    public SegmentoDto toDto(Segmento segmento) {
        SegmentoDto dto = new SegmentoDto();
        dto.setId(segmento.getId());
        dto.setNombre(segmento.getNombre());
        dto.setDescripcion(segmento.getDescripcion());
        dto.setTipoAudiencia(segmento.getTipoAudiencia());
        dto.setEstado(segmento.getEstado());
        dto.setFechaCreacion(segmento.getFechaCreacion());
        dto.setFechaActualizacion(segmento.getFechaActualizacion());

        // Use cantidadMiembros from domain instead of querying database
        dto.setCantidadMiembros(segmento.getCantidadMiembros() != null ? segmento.getCantidadMiembros() : 0);

        if (segmento.getReglaPrincipal() != null) {
            dto.setReglaPrincipal(toDto(segmento.getReglaPrincipal()));
        }

        return dto;
    }

    public Segmento toDomain(SegmentoDto dto) {
        Segmento segmento = new Segmento();
        segmento.setId(dto.getId());
        segmento.setNombre(dto.getNombre());
        segmento.setDescripcion(dto.getDescripcion());
        segmento.setTipoAudiencia(dto.getTipoAudiencia());

        // Ensure INACTIVO as default for new segments
        if (dto.getEstado() != null && !dto.getEstado().isEmpty()) {
            segmento.setEstado(dto.getEstado());
        }
        // If estado is null/empty, Segmento constructor already sets INACTIVO

        if (dto.getReglaPrincipal() != null) {
            segmento.setReglaPrincipal(toDomain(dto.getReglaPrincipal()));
        }

        return segmento;
    }

    private SegmentoDto.ReglaDto toDto(ReglaSegmento regla) {
        SegmentoDto.ReglaDto dto = new SegmentoDto.ReglaDto();
        if (regla instanceof ReglaSimple) {
            dto.setTipo("SIMPLE");
            ReglaSimple simple = (ReglaSimple) regla;
            dto.setIdFiltro(simple.getIdFiltro());
            dto.setCampo(simple.getCampo());
            dto.setOperador(simple.getOperador());
            dto.setValorTexto(simple.getValorTexto());
        } else if (regla instanceof GrupoReglasAnd) {
            dto.setTipo("AND");
            dto.setReglas(((GrupoReglasAnd) regla).getReglas().stream()
                    .map(this::toDto).collect(Collectors.toList()));
        } else if (regla instanceof GrupoReglasOr) {
            dto.setTipo("OR");
            dto.setReglas(((GrupoReglasOr) regla).getReglas().stream()
                    .map(this::toDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private ReglaSegmento toDomain(SegmentoDto.ReglaDto dto) {
        if ("SIMPLE".equals(dto.getTipo())) {
            ReglaSimple regla = new ReglaSimple();
            regla.setIdFiltro(dto.getIdFiltro());
            regla.setCampo(dto.getCampo());
            regla.setOperador(dto.getOperador());
            regla.setValorTexto(dto.getValorTexto());
            return regla;
        } else if ("AND".equals(dto.getTipo())) {
            GrupoReglasAnd grupo = new GrupoReglasAnd();
            if (dto.getReglas() != null) {
                dto.getReglas().forEach(r -> grupo.addRegla(toDomain(r)));
            }
            return grupo;
        } else if ("OR".equals(dto.getTipo())) {
            GrupoReglasOr grupo = new GrupoReglasOr();
            if (dto.getReglas() != null) {
                dto.getReglas().forEach(r -> grupo.addRegla(toDomain(r)));
            }
            return grupo;
        }
        return null;
    }
}

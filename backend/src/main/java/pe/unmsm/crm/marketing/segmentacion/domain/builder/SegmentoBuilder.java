package pe.unmsm.crm.marketing.segmentacion.domain.builder;

import pe.unmsm.crm.marketing.segmentacion.domain.model.Segmento;
import pe.unmsm.crm.marketing.segmentacion.domain.model.ReglaSegmento;

public class SegmentoBuilder {
    private Segmento segmento;

    public SegmentoBuilder() {
        this.segmento = new Segmento();
    }

    public SegmentoBuilder nombre(String nombre) {
        this.segmento.setNombre(nombre);
        return this;
    }

    public SegmentoBuilder descripcion(String descripcion) {
        this.segmento.setDescripcion(descripcion);
        return this;
    }

    public SegmentoBuilder tipoAudiencia(String tipoAudiencia) {
        this.segmento.setTipoAudiencia(tipoAudiencia);
        return this;
    }

    public SegmentoBuilder reglaPrincipal(ReglaSegmento regla) {
        this.segmento.setReglaPrincipal(regla);
        return this;
    }

    public Segmento build() {
        return this.segmento;
    }
}

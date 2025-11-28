package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

public enum EstadoCampanaMailing {
    PENDIENTE(1, "pendiente"),
    LISTO(2, "listo"),
    ENVIADO(3, "enviado"),
    VENCIDO(4, "vencido"),
    FINALIZADO(5, "finalizado"),
    CANCELADO(6, "cancelado");

    private final Integer id;
    private final String nombre;

    EstadoCampanaMailing(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public static EstadoCampanaMailing fromId(Integer id) {
        for (EstadoCampanaMailing estado : values()) {
            if (estado.id.equals(id)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + id);
    }
}

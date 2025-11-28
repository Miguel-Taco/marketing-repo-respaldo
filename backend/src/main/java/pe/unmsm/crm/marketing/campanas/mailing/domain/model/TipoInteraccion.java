package pe.unmsm.crm.marketing.campanas.mailing.domain.model;

public enum TipoInteraccion {
    APERTURA(1, "apertura"),
    CLIC(2, "clic"),
    REBOTE(3, "rebote"),
    BAJA(4, "baja");

    private final Integer id;
    private final String nombre;

    TipoInteraccion(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public static TipoInteraccion fromId(Integer id) {
        for (TipoInteraccion tipo : values()) {
            if (tipo.id.equals(id)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de interacción desconocido: " + id);
    }
}
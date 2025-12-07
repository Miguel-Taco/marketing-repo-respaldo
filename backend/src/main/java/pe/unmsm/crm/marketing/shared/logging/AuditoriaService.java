package pe.unmsm.crm.marketing.shared.logging;

public interface AuditoriaService {
    void registrarEvento(ModuloLog modulo, AccionLog accion, Long idEntidad, Long idUsuario, String detalle);
}

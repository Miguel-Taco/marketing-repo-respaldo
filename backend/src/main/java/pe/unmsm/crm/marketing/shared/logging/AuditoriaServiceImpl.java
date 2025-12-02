package pe.unmsm.crm.marketing.shared.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de auditoría.
 * Escribe los eventos en un logger específico llamado "AUDITORIA".
 */
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void registrarEvento(ModuloLog modulo, AccionLog accion, Long idEntidad, Long idUsuario, String detalle) {
        // Formato estructurado: modulo={}, accion={}, entidadId={}, usuarioId={},
        // detalle={}
        logger.info("modulo={}, accion={}, entidadId={}, usuarioId={}, detalle={}",
                modulo, accion, idEntidad, idUsuario, detalle);
    }
}

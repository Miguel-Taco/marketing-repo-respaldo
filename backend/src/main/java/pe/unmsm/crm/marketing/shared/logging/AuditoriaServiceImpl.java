package pe.unmsm.crm.marketing.shared.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void registrarEvento(ModuloLog modulo, AccionLog accion, Long idEntidad, Long idUsuario, String detalle) {
        logger.info("modulo={}, accion={}, entidadId={}, usuarioId={}, detalle={}",
                modulo, accion, idEntidad, idUsuario, detalle);
    }
}

package pe.unmsm.crm.marketing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.mailing.domain.model.CampanaMailing;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaCampanaMailingRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.AgenteMarketingEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.AgenteMarketingRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.CampaniaAgenteRepository;
import pe.unmsm.crm.marketing.security.domain.UsuarioEntity;
import pe.unmsm.crm.marketing.security.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthorizationService {

    private final UsuarioRepository usuarioRepository;
    private final AgenteMarketingRepository agenteMarketingRepository;
    private final CampaniaAgenteRepository campaniaAgenteRepository;
    private final JpaCampanaMailingRepository campanaMailingRepository;

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    public Optional<AgenteMarketingEntity> getCurrentAgente() {
        return getCurrentUsuario()
                .flatMap(usuario -> agenteMarketingRepository.findByIdUsuario(usuario.getIdUsuario()));
    }

    public Integer requireCurrentAgentId() {
        return getCurrentAgente()
                .map(AgenteMarketingEntity::getIdAgente)
                .orElseThrow(() -> new AccessDeniedException("El usuario no tiene un agente asignado"));
    }

    public Integer resolveAgentId(Long requestedAgentId) {
        if (requestedAgentId == null) {
            return isAdmin() ? null : requireCurrentAgentId();
        }
        return resolveAgentId(requestedAgentId.intValue());
    }

    public Integer resolveAgentId(Integer requestedAgentId) {
        if (requestedAgentId == null) {
            return isAdmin() ? null : requireCurrentAgentId();
        }
        if (isAdmin()) {
            return requestedAgentId;
        }
        Integer current = requireCurrentAgentId();
        if (!requestedAgentId.equals(current)) {
            throw new AccessDeniedException("No tienes acceso a este agente");
        }
        return current;
    }

    public Integer ensureAgentAccess(Long requestedAgentId) {
        Integer resolved = resolveAgentId(requestedAgentId);
        if (resolved == null) {
            throw new AccessDeniedException("Debes especificar un agente");
        }
        return resolved;
    }

    public void ensureCampaniaTelefonicaAccess(Long idCampania) {
        if (idCampania == null) {
            throw new AccessDeniedException("Id de campaña inválido");
        }
        if (isAdmin()) {
            return;
        }
        Integer agenteId = requireCurrentAgentId();
        boolean asignado = campaniaAgenteRepository.existsByIdCampaniaAndIdAgente(idCampania.intValue(), agenteId);
        if (!asignado) {
            log.warn("Agente {} intentó acceder a campaña telefónica {} sin permisos", agenteId, idCampania);
            throw new AccessDeniedException("No tienes acceso a esta campaña telefónica");
        }
    }

    public void ensureCampaniaMailingAccess(Integer idCampania) {
        if (idCampania == null) {
            throw new AccessDeniedException("Id de campa?a inv?lido");
        }
        if (isAdmin()) {
            return;
        }
        Integer agenteId = requireCurrentAgentId();
        boolean asignado = campanaMailingRepository.existsByIdAndIdAgenteAsignado(idCampania, agenteId);
        if (!asignado) {
            log.warn("Agente {} intent? acceder a campa?a de mailing {} sin permisos", agenteId, idCampania);
            throw new AccessDeniedException("No tienes acceso a esta campa?a de mailing");
        }
    }

    public List<Integer> loadMailingCampaignIds(Integer agenteId) {
        if (agenteId == null) {
            if (isAdmin()) {
                return campanaMailingRepository.findAll().stream()
                        .map(CampanaMailing::getId)
                        .toList();
            }
            return List.of();
        }
        return campanaMailingRepository.findIdsByIdAgenteAsignado(agenteId);
    }

    private Optional<UsuarioEntity> getCurrentUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return usuarioRepository.findByUsername(authentication.getName());
    }

    public UsuarioEntity requireCurrentUsuario() {
        return getCurrentUsuario()
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}

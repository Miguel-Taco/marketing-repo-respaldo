package pe.unmsm.crm.marketing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.unmsm.crm.marketing.campanas.mailing.infra.persistence.repository.JpaCampanaMailingRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.AgenteMarketingEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.AgenteMarketingRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.CampaniaAgenteRepository;
import pe.unmsm.crm.marketing.security.api.dto.LoginRequest;
import pe.unmsm.crm.marketing.security.api.dto.LoginResponse;
import pe.unmsm.crm.marketing.security.api.dto.UserInfoDTO;
import pe.unmsm.crm.marketing.security.domain.UsuarioEntity;
import pe.unmsm.crm.marketing.security.jwt.JwtTokenProvider;
import pe.unmsm.crm.marketing.security.repository.UsuarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación que maneja login y generación de tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final AgenteMarketingRepository agenteMarketingRepository;
    private final CampaniaAgenteRepository campaniaAgenteRepository;
    private final JpaCampanaMailingRepository campanaMailingRepository;

    /**
     * Autentica al usuario y genera un token JWT
     *
     * @param loginRequest datos de login (username, password)
     * @return respuesta con token y datos del usuario
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Intentando autenticar usuario: {}", loginRequest.getUsername());

        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener el usuario de la BD
        UsuarioEntity usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Generar token JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails, usuario.getIdUsuario());

        log.info("Usuario autenticado exitosamente: {}", loginRequest.getUsername());

        UserInfoDTO userInfo = buildUserInfo(usuario);

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(usuario.getUsername())
                .userId(usuario.getIdUsuario())
                .roles(userInfo.getRoles())
                .agentId(userInfo.getAgentId())
                .canalPrincipal(userInfo.getCanalPrincipal())
                .puedeAccederMailing(userInfo.getPuedeAccederMailing())
                .puedeAccederTelefonia(userInfo.getPuedeAccederTelefonia())
                .campaniasMailing(userInfo.getCampaniasMailing())
                .campaniasTelefonicas(userInfo.getCampaniasTelefonicas())
                .profile(userInfo)
                .build();
    }

    /**
     * Obtiene la información del usuario actualmente autenticado
     *
     * @return información del usuario
     */
    public UserInfoDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        String username = authentication.getName();
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return buildUserInfo(usuario);
    }

    private UserInfoDTO buildUserInfo(UsuarioEntity usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toList());

        boolean isAdmin = roles.contains("ADMIN");

        AgenteMarketingEntity agente = agenteMarketingRepository.findByIdUsuario(usuario.getIdUsuario())
                .orElse(null);

        Long agentId = agente != null ? agente.getIdAgente().longValue() : null;
        String canalPrincipal = agente != null ? agente.getCanalPrincipal() : null;

        List<Long> campaniasTelefonicas = agentId != null
                ? campaniaAgenteRepository.findCampaniaIdsByAgente(agentId.intValue()).stream()
                        .map(Integer::longValue)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        List<Long> campaniasMailing = agentId != null
                ? campanaMailingRepository.findIdsByIdAgenteAsignado(agentId.intValue()).stream()
                        .map(Integer::longValue)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        boolean puedeAccederTelefonia = isAdmin || !campaniasTelefonicas.isEmpty();
        boolean puedeAccederMailing = isAdmin || !campaniasMailing.isEmpty();

        return UserInfoDTO.builder()
                .userId(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .activo(usuario.getActivo())
                .roles(roles)
                .agentId(agentId)
                .canalPrincipal(canalPrincipal)
                .puedeAccederMailing(puedeAccederMailing)
                .puedeAccederTelefonia(puedeAccederTelefonia)
                .campaniasMailing(campaniasMailing)
                .campaniasTelefonicas(campaniasTelefonicas)
                .build();
    }
}

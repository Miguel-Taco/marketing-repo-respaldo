package pe.unmsm.crm.marketing.campanas.encuestas.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.CreateEncuestaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.api.dto.EncuestaCompletaDto;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.model.Encuesta;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.EncuestaRepository;
import pe.unmsm.crm.marketing.campanas.encuestas.domain.repository.CampanaExternalRepository;
import pe.unmsm.crm.marketing.security.domain.UsuarioEntity;
import pe.unmsm.crm.marketing.security.service.UserAuthorizationService;
import pe.unmsm.crm.marketing.shared.logging.AccionLog;
import pe.unmsm.crm.marketing.shared.logging.AuditoriaService;
import pe.unmsm.crm.marketing.shared.logging.ModuloLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncuestaServiceTest {

    @Mock
    private EncuestaRepository encuestaRepository;

    @Mock
    private CampanaExternalRepository campanaRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @InjectMocks
    private EncuestaService encuestaService;

    private UsuarioEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UsuarioEntity();
        mockUser.setIdUsuario(1L);
        mockUser.setUsername("testuser");
    }

    @Test
    void crearEncuesta_ShouldLogEvent() {
        // Arrange
        CreateEncuestaDto dto = new CreateEncuestaDto();
        dto.setTitulo("Test Encuesta");
        dto.setDescripcion("Descripcion");
        dto.setEstado(Encuesta.EstadoEncuesta.BORRADOR);
        dto.setPreguntas(new ArrayList<>());

        Encuesta savedEncuesta = new Encuesta();
        savedEncuesta.setIdEncuesta(100);
        savedEncuesta.setTitulo("Test Encuesta");
        savedEncuesta.setDescripcion("Descripcion");
        savedEncuesta.setEstado(Encuesta.EstadoEncuesta.BORRADOR);
        savedEncuesta.setPreguntas(new ArrayList<>());

        when(encuestaRepository.save(any(Encuesta.class))).thenReturn(savedEncuesta);
        when(userAuthorizationService.requireCurrentUsuario()).thenReturn(mockUser);

        // Act
        encuestaService.crearEncuesta(dto);

        // Assert
        verify(auditoriaService).registrarEvento(
                eq(ModuloLog.ENCUESTAS),
                eq(AccionLog.CREAR),
                eq(100L),
                eq(1L),
                contains("Encuesta creada: 'Test Encuesta'"));
    }

    @Test
    void actualizarEncuesta_ShouldLogEvent() {
        // Arrange
        Integer id = 100;
        CreateEncuestaDto dto = new CreateEncuestaDto();
        dto.setTitulo("Updated Encuesta");
        dto.setDescripcion("Updated Desc");
        dto.setEstado(Encuesta.EstadoEncuesta.ACTIVA);
        dto.setPreguntas(new ArrayList<>());

        Encuesta existingEncuesta = new Encuesta();
        existingEncuesta.setIdEncuesta(id);
        existingEncuesta.setTitulo("Old Encuesta");
        existingEncuesta.setPreguntas(new ArrayList<>());

        Encuesta updatedEncuesta = new Encuesta();
        updatedEncuesta.setIdEncuesta(id);
        updatedEncuesta.setTitulo("Updated Encuesta");
        updatedEncuesta.setPreguntas(new ArrayList<>());

        when(encuestaRepository.findById(id)).thenReturn(Optional.of(existingEncuesta));
        when(encuestaRepository.save(any(Encuesta.class))).thenReturn(updatedEncuesta);
        when(userAuthorizationService.requireCurrentUsuario()).thenReturn(mockUser);

        // Act
        encuestaService.actualizarEncuesta(id, dto);

        // Assert
        verify(auditoriaService).registrarEvento(
                eq(ModuloLog.ENCUESTAS),
                eq(AccionLog.ACTUALIZAR),
                eq(100L),
                eq(1L),
                contains("Encuesta actualizada: 'Updated Encuesta'"));
    }

    @Test
    void archivarEncuesta_ShouldLogEvent() {
        // Arrange
        Integer id = 100;
        Encuesta encuesta = new Encuesta();
        encuesta.setIdEncuesta(id);
        encuesta.setEstado(Encuesta.EstadoEncuesta.ACTIVA);

        when(encuestaRepository.findById(id)).thenReturn(Optional.of(encuesta));
        when(campanaRepository.findByIdEncuesta(id)).thenReturn(Collections.emptyList());
        when(userAuthorizationService.requireCurrentUsuario()).thenReturn(mockUser);

        // Act
        encuestaService.archivarEncuesta(id);

        // Assert
        verify(auditoriaService).registrarEvento(
                eq(ModuloLog.ENCUESTAS),
                eq(AccionLog.CAMBIAR_ESTADO),
                eq(100L),
                eq(1L),
                contains("Encuesta archivada (ID: 100)"));
    }
}

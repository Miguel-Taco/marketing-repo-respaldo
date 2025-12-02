package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import pe.unmsm.crm.marketing.campanas.telefonicas.api.dto.ContactoDTO;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.CampaniaTelefonicaEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity.ColaLlamadaEntity;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.mapper.CampaignMapper;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.CampaniaTelefonicaRepository;
import pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.repository.ColaLlamadaRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaCampaignDataProviderTest {

    @Mock
    private CampaniaTelefonicaRepository campaniaRepo;

    @Mock
    private ColaLlamadaRepository colaRepo;

    @Mock
    private CampaignMapper mapper;

    @InjectMocks
    private JpaCampaignDataProvider dataProvider;

    @Test
    void obtenerSiguienteContacto_ShouldReturnNull_WhenCampaignIsPaused() {
        // Arrange
        Long idCampania = 1L;
        Long idAgente = 10L;

        CampaniaTelefonicaEntity pausedCampaign = new CampaniaTelefonicaEntity();
        pausedCampaign.setId(1);
        pausedCampaign.setEstado("Pausada");
        pausedCampaign.setIdEstado(3); // PAUSADA

        when(campaniaRepo.findById(1)).thenReturn(Optional.of(pausedCampaign));

        // Act
        ContactoDTO result = dataProvider.obtenerSiguienteContacto(idCampania, idAgente);

        // Assert
        assertNull(result, "Should return null when campaign is paused");
    }

    @Test
    void obtenerSiguienteContacto_ShouldReturnNull_WhenCampaignIsCancelled() {
        // Arrange
        Long idCampania = 1L;
        Long idAgente = 10L;

        CampaniaTelefonicaEntity cancelledCampaign = new CampaniaTelefonicaEntity();
        cancelledCampaign.setId(1);
        cancelledCampaign.setEstado("Cancelada");
        cancelledCampaign.setIdEstado(5); // CANCELADA

        when(campaniaRepo.findById(1)).thenReturn(Optional.of(cancelledCampaign));

        // Act
        ContactoDTO result = dataProvider.obtenerSiguienteContacto(idCampania, idAgente);

        // Assert
        assertNull(result, "Should return null when campaign is cancelled");
    }

    @Test
    void obtenerSiguienteContacto_ShouldReturnContact_WhenCampaignIsActive() {
        // Arrange
        Long idCampania = 1L;
        Long idAgente = 10L;

        CampaniaTelefonicaEntity activeCampaign = new CampaniaTelefonicaEntity();
        activeCampaign.setId(1);
        activeCampaign.setEstado("Vigente");
        activeCampaign.setIdEstado(2); // VIGENTE

        ColaLlamadaEntity contactEntity = new ColaLlamadaEntity();
        contactEntity.setId(100);

        ContactoDTO contactDTO = new ContactoDTO();
        contactDTO.setId(100L);

        when(campaniaRepo.findById(1)).thenReturn(Optional.of(activeCampaign));
        when(colaRepo.findNextAvailableContact(anyInt(), anyInt(), any(PageRequest.class)))
                .thenReturn(List.of(contactEntity));
        when(mapper.toContactoDTO(contactEntity)).thenReturn(contactDTO);

        // Act
        ContactoDTO result = dataProvider.obtenerSiguienteContacto(idCampania, idAgente);

        // Assert
        assertNotNull(result, "Should return contact when campaign is active");
    }
}

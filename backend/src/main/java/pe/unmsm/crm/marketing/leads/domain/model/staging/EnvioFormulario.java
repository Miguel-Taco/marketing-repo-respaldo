package pe.unmsm.crm.marketing.leads.domain.model.staging;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoCaptacion;
import pe.unmsm.crm.marketing.shared.domain.BaseEntity;

import java.io.IOException;
import java.util.Map;

@Entity
@Table(name = "envios_formulario")
@Data // Lombok genera Getters, Setters, toString, etc.
@NoArgsConstructor // Constructor vacío requerido por JPA
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AttributeOverride(name = "id", column = @Column(name = "envio_id")) // Mapea 'id' a 'envio_id'
public class EnvioFormulario extends BaseEntity {

    // IMPORTANTE: En tu SQL esta columna es NOT NULL.
    // Le ponemos un valor por defecto (1 = EN_PROCESO) para que no falle al
    // guardar.
    @Column(name = "estado_proceso_id", nullable = false)
    private EstadoCaptacion estadoProceso = EstadoCaptacion.EN_PROCESO;

    // --- MANEJO DE JSON ---

    // Esta es la columna real en la BD (String/JSON)
    @Column(name = "datos_formulario", columnDefinition = "json")
    private String datosJson;

    // Este campo NO se guarda en BD, sirve para trabajar en Java
    @Transient
    private Map<String, String> respuestas;

    // Antes de guardar en BD: Convierte el Map -> String JSON
    @PrePersist
    @PreUpdate
    public void convertMapToJson() {
        if (this.respuestas != null) {
            try {
                this.datosJson = new ObjectMapper().writeValueAsString(this.respuestas);
            } catch (IOException e) {
                throw new RuntimeException("Error al serializar respuestas a JSON", e);
            }
        }
    }

    // Al leer de BD: Convierte String JSON -> Map
    @PostLoad
    public void convertJsonToMap() {
        if (this.datosJson != null) {
            try {
                this.respuestas = new ObjectMapper().readValue(this.datosJson,
                        new TypeReference<Map<String, String>>() {
                        });
            } catch (IOException e) {
                System.err.println("Error al deserializar JSON de BD: " + e.getMessage());
            }
        }
    }

    // Método helper para facilitar el uso desde los servicios
    public void setRespuestas(Map<String, String> respuestas) {
        this.respuestas = respuestas;
        this.convertMapToJson(); // Actualiza el JSON interno automáticamente
    }
}
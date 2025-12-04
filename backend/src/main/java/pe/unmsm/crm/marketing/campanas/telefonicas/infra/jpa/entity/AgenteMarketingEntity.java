package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agente_marketing", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenteMarketingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agente")
    private Integer idAgente;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_trabajador_rrhh")
    private Long idTrabajadorRrhh;

    @Column(name = "extension_telefono")
    private String extensionTelefono;

    @Column(name = "canal_principal")
    private String canalPrincipal;

    @Column(name = "max_llamadas_dia")
    private Integer maxLlamadasDia;

    @Column(name = "habilitado_llamadas")
    private Boolean habilitadoLlamadas;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}

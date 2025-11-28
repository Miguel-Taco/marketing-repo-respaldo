package pe.unmsm.crm.marketing.campanas.telefonicas.infra.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "campania_telefonica_config", schema = "railway")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaniaTelefonicaConfigEntity {

    @Id
    @Column(name = "id_campania_telefonica")
    private Integer idCampaniaTelefonica;

    @Column(name = "hora_inicio_permitida", nullable = false)
    private LocalTime horaInicioPermitida = LocalTime.of(9, 0);

    @Column(name = "hora_fin_permitida", nullable = false)
    private LocalTime horaFinPermitida = LocalTime.of(21, 0);

    @Column(name = "dias_semana_permitidos", length = 50)
    private String diasSemanaPermitidos = "LUN-MAR-MIE-JUE-VIE";

    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos = 3;

    @Column(name = "intervalo_reintentos_min", nullable = false)
    private Integer intervaloReintentosMin = 60;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_discado", nullable = false)
    private TipoDiscadoEnum tipoDiscado = TipoDiscadoEnum.Manual;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_contacto", nullable = false)
    private ModoContactoEnum modoContacto = ModoContactoEnum.Llamada;

    @Column(name = "permite_sms_respaldo", nullable = false)
    private Boolean permiteSmsRespaldo = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campania_telefonica", insertable = false, updatable = false)
    private CampaniaTelefonicaEntity campania;

    public enum TipoDiscadoEnum {
        Manual, Preview, Progresivo
    }

    public enum ModoContactoEnum {
        Llamada, Llamada_SMS
    }
}

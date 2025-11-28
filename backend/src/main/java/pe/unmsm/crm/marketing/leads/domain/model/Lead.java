package pe.unmsm.crm.marketing.leads.domain.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import pe.unmsm.crm.marketing.leads.domain.vo.DatosContacto;
import pe.unmsm.crm.marketing.leads.domain.vo.DatosDemograficos;
import pe.unmsm.crm.marketing.leads.domain.vo.TrackingUTM;
import pe.unmsm.crm.marketing.leads.domain.enums.EstadoLead;
import pe.unmsm.crm.marketing.leads.domain.enums.TipoFuente;
import pe.unmsm.crm.marketing.shared.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "leads")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "fuente_tipo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) // Importante para Lombok con herencia
@AttributeOverride(name = "id", column = @Column(name = "lead_id")) // Mapea 'id' del padre a 'lead_id' de la tabla
public abstract class Lead extends BaseEntity {

        @Column(name = "nombre_completo")
        protected String nombre;

        @Column(name = "fecha_creacion")
        protected LocalDateTime fechaCreacion = LocalDateTime.now();

        @Column(name = "estado_lead_id")
        protected EstadoLead estado = EstadoLead.NUEVO;

        @Embedded
        @AttributeOverrides({
                        @AttributeOverride(name = "email", column = @Column(name = "email", unique = true)),
                        @AttributeOverride(name = "telefono", column = @Column(name = "telefono", unique = true))
        })
        protected DatosContacto contacto;

        @Embedded
        @AttributeOverrides({
                        @AttributeOverride(name = "edad", column = @Column(name = "edad")),
                        @AttributeOverride(name = "genero", column = @Column(name = "genero")),
                        @AttributeOverride(name = "distrito", column = @Column(name = "distrito_id")),
                        @AttributeOverride(name = "nivelEducativo", column = @Column(name = "nivel_educativo")),
                        @AttributeOverride(name = "estadoCivil", column = @Column(name = "estado_civil"))
        })
        protected DatosDemograficos demograficos;

        @Embedded
        @AttributeOverrides({
                        @AttributeOverride(name = "source", column = @Column(name = "utm_source")),
                        @AttributeOverride(name = "medium", column = @Column(name = "utm_medium")),
                        @AttributeOverride(name = "campaign", column = @Column(name = "utm_campaign")),
                        @AttributeOverride(name = "term", column = @Column(name = "utm_term")),
                        @AttributeOverride(name = "content", column = @Column(name = "utm_content"))
        })
        protected TrackingUTM tracking;

        @Column(name = "fuente_tipo", insertable = false, updatable = false)
        @Enumerated(EnumType.STRING)
        protected TipoFuente fuenteTipo;

        @Transient
        protected Long idReferenciaOrigen;

        @Column(name = "envio_formulario_id")
        protected Long envioFormularioId;

        @Column(name = "registro_importado_id")
        protected Long registroImportadoId;

        public abstract String getResumenOrigen();
}
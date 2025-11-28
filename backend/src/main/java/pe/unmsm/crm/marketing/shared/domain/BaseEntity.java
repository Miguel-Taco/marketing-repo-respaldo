package pe.unmsm.crm.marketing.shared.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Recomendado para JPA
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED) // si quieres permitir que JPA/la propia entidad lo cambien
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

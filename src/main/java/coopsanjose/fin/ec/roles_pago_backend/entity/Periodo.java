package coopsanjose.fin.ec.roles_pago_backend.entity;

import coopsanjose.fin.ec.roles_pago_backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "periodo", uniqueConstraints = @UniqueConstraint(columnNames = {"periodo"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Periodo extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicado_por")
    private Usuario publicadoPor;

    /*
    @CreatedBy
    @Column(name = "PUBLICADOPOR", nullable = false, updatable = false)
    private String publicadoPor;
    *
    * */

    @Column(name = "periodo", nullable = false)
    private LocalDate fechaPeriodo; // siempre el dia 1 del mes, ej. 2026-08-01

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPeriodo estado = EstadoPeriodo.CARGADO;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}

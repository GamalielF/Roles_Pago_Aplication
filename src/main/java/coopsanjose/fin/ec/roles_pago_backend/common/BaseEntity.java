package coopsanjose.fin.ec.roles_pago_backend.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

   /* @CreatedDate
    @Column(name="CREADO_EN", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADOREGISTRO", nullable = false)
    private EstadoRegistro estadoRegistro=EstadoRegistro.ACTIVO;

    public enum EstadoRegistro {
        ACTIVO,
        INACTIVO
    } */

}

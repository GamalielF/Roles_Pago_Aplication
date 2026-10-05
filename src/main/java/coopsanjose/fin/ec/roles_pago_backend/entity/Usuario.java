package coopsanjose.fin.ec.roles_pago_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, unique = true, length = 10)
    private String cedula;

    @Column(name = "username_ad", nullable = false, unique = true, length = 50)
    private String usernameAd;

    @Column(length = 120)
    private String email;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    @Column(length = 100)
    private String cargo;

    @Column(length = 100)
    private String departamento;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_primer_acceso")
    private LocalDateTime fechaPrimerAcceso;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UsuarioRol> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public boolean tieneRol(Rol rol) {
        return roles.stream().anyMatch(ur -> ur.getRol() == rol);
    }
}

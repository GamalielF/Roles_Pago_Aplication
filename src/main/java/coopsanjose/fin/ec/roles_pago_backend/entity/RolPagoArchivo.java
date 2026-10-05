package coopsanjose.fin.ec.roles_pago_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rol_pago_archivo", indexes = @Index(name = "idx_rolpago_cedula", columnList = "cedula"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RolPagoArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idArchivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo", nullable = false)
    private Periodo periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por", nullable = false)
    private Usuario subidoPor;

    @Column(nullable = false, length = 10)
    private String cedula;

    @Column(name = "nombre_archivo_original", length = 150)
    private String nombreArchivoOriginal;

    @Column(name = "ruta_almacenamiento", nullable = false, length = 255)
    private String rutaAlmacenamiento; // ruta RELATIVA, ej. 2026-08/0202125712.pdf

    @Column(name = "hash_archivo", length = 64)
    private String hashArchivo;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "nombre_extraido_pdf", length = 150)
    private String nombreExtraidoPdf;

    @Column(name = "mes_extraido_pdf", length = 20)
    private String mesExtraidoPdf;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 20)
    private EstadoValidacion estadoValidacion;

    @Column(name = "detalle_validacion", length = 255)
    private String detalleValidacion;

    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
    }
}

package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.dto.UsuarioLoginRequest;
import coopsanjose.fin.ec.roles_pago_backend.dto.UsuarioResponse;
import coopsanjose.fin.ec.roles_pago_backend.entity.ResultadoAuditoria;
import coopsanjose.fin.ec.roles_pago_backend.entity.Rol;
import coopsanjose.fin.ec.roles_pago_backend.entity.TipoEvento;
import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.entity.UsuarioRol;
import coopsanjose.fin.ec.roles_pago_backend.repository.UsuarioRepository;
import coopsanjose.fin.ec.roles_pago_backend.repository.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final AuditoriaService auditoriaService;

    // Username del primer ADMIN (ver application.properties). Si la propiedad
    // no existe, queda vacio y el bootstrap simplemente no se aplica.
    @Value("${app.bootstrap.admin-username:}")
    private String adminInicial;

    // Resultado interno: la entidad + si fue su primer acceso.
    private record ResolucionUsuario(Usuario usuario, boolean primerAcceso) {}

    private ResolucionUsuario resolver(UsuarioLoginRequest request, String ip, String userAgent) {

        Optional<Usuario> existente = usuarioRepository.findByUsernameAd(request.getUsernameAd());
        boolean esPrimerAcceso = existente.isEmpty();
        Usuario usuario;

        if (esPrimerAcceso) {
            usuario = Usuario.builder()
                    .usernameAd(request.getUsernameAd())
                    .email(request.getEmail())
                    .nombreCompleto(request.getNombreCompleto())
                    .cargo(request.getCargo())
                    .departamento(request.getDepartamento())
                    .cedula(request.getCedula())
                    .fechaPrimerAcceso(LocalDateTime.now())
                    .activo(true)
                    .build();
            usuario = usuarioRepository.save(usuario);

            // Todo usuario nuevo nace como EMPLEADO (asignado por el sistema)
            UsuarioRol rolEmpleado = UsuarioRol.builder()
                    .usuario(usuario)
                    .rol(Rol.EMPLEADO)
                    .asignadoPor(null)
                    .build();
            usuarioRolRepository.save(rolEmpleado);
            usuario.getRoles().add(rolEmpleado);

            auditoriaService.registrar(usuario, TipoEvento.PRIMER_LOGIN, Rol.EMPLEADO,
                    null, null, ResultadoAuditoria.EXITOSO, ip, userAgent);

        } else {
            usuario = existente.get();
            usuario.setEmail(request.getEmail());
            usuario.setNombreCompleto(request.getNombreCompleto());
            usuario.setCargo(request.getCargo());
            usuario.setDepartamento(request.getDepartamento());
            // La cedula solo se escribe si aun esta vacia (no se sobrescribe)
            if (request.getCedula() != null && usuario.getCedula() == null) {
                usuario.setCedula(request.getCedula());
            }
            usuario = usuarioRepository.save(usuario);

            auditoriaService.registrar(usuario, TipoEvento.LOGIN, null,
                    null, null, ResultadoAuditoria.EXITOSO, ip, userAgent);
        }

        // Bootstrap del primer ADMIN: resuelve el "huevo y la gallina"
        // (el primer admin no puede ser asignado por otro admin inexistente).
        if (!adminInicial.isBlank()
                && adminInicial.equalsIgnoreCase(usuario.getUsernameAd())
                && !usuario.tieneRol(Rol.ADMIN)) {
            UsuarioRol rolAdmin = UsuarioRol.builder()
                    .usuario(usuario)
                    .rol(Rol.ADMIN)
                    .asignadoPor(null)
                    .build();
            usuarioRolRepository.save(rolAdmin);
            usuario.getRoles().add(rolAdmin);
        }

        // CORRECCION DEL LazyInitializationException:
        // fuerza la carga de los roles MIENTRAS la transaccion sigue abierta.
        // Sin esto, UsuarioAuthenticationToken falla al leer usuario.getRoles()
        // en usuarios existentes (la coleccion es LAZY y ya no hay sesion).
        Hibernate.initialize(usuario.getRoles());

        return new ResolucionUsuario(usuario, esPrimerAcceso);
    }

    /** Usado por los filtros de seguridad (mock o JWT) para poblar el SecurityContext. */
    @Transactional
    public Usuario resolverUsuarioEntity(UsuarioLoginRequest request, String ip, String userAgent) {
        return resolver(request, ip, userAgent).usuario();
    }

    /** Usado por el endpoint de prueba /api/auth/resolver (llamada manual desde Postman). */
    @Transactional
    public UsuarioResponse resolverUsuario(UsuarioLoginRequest request, String ip, String userAgent) {
        ResolucionUsuario r = resolver(request, ip, userAgent);
        List<Rol> roles = r.usuario().getRoles().stream().map(UsuarioRol::getRol).toList();

        return UsuarioResponse.builder()
                .idUsuario(r.usuario().getIdUsuario())
                .cedula(r.usuario().getCedula())
                .usernameAd(r.usuario().getUsernameAd())
                .email(r.usuario().getEmail())
                .nombreCompleto(r.usuario().getNombreCompleto())
                .roles(roles)
                .primerAcceso(r.primerAcceso())
                .build();
    }
}
package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.entity.*;
import coopsanjose.fin.ec.roles_pago_backend.repository.AuditoriaAccesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaAccesoRepository auditoriaAccesoRepository;
    private static final long VENTANA_LOGIN_MINUTOS = 30;
    public void registrarLogin(Usuario usuario, String ip, String userAgent) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(VENTANA_LOGIN_MINUTOS);
        boolean reciente = auditoriaAccesoRepository
                .existsByUsuario_IdUsuarioAndTipoEventoInAndFechaHoraAfter(
                        usuario.getIdUsuario(),
                        List.of(TipoEvento.LOGIN, TipoEvento.PRIMER_LOGIN), desde);
        if (!reciente) {
            registrar(usuario, TipoEvento.LOGIN, null, null, null,
                    ResultadoAuditoria.EXITOSO, ip, userAgent);
        }
    }
    public void registrar(Usuario usuario, TipoEvento tipoEvento, Rol rolUtilizado,
                          String cedulaConsultada, Periodo periodoConsultado,
                          ResultadoAuditoria resultado, String ipOrigen, String userAgent) {

        AuditoriaAcceso registro = AuditoriaAcceso.builder()
                .usuario(usuario)
                .tipoEvento(tipoEvento)
                .rolUtilizado(rolUtilizado)
                .cedulaConsultada(cedulaConsultada)
                .periodoConsultado(periodoConsultado)
                .resultado(resultado)
                .ipOrigen(ipOrigen)
                .userAgent(userAgent)
                .build();

        auditoriaAccesoRepository.save(registro);
    }
}

package coopsanjose.fin.ec.roles_pago_backend.service;

import coopsanjose.fin.ec.roles_pago_backend.entity.*;
import coopsanjose.fin.ec.roles_pago_backend.repository.AuditoriaAccesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaAccesoRepository auditoriaAccesoRepository;

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

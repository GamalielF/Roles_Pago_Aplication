package coopsanjose.fin.ec.roles_pago_backend.controller;

import coopsanjose.fin.ec.roles_pago_backend.dto.RolPagoContenidoDto;
import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.service.RolPagoConsultaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles-pago")
@RequiredArgsConstructor
public class RolPagoConsultaController {

    private final RolPagoConsultaService consultaService;

    /** GET /api/roles-pago/0202125712/periodos  ->  ["2026-08", "2026-07", ...] */
    @GetMapping("/{cedula}/periodos")
    public List<String> periodos(@PathVariable String cedula,
                                 @AuthenticationPrincipal Usuario usuario,
                                 HttpServletRequest req) {
        return consultaService.periodosDisponibles(cedula, usuario,
                req.getRemoteAddr(), req.getHeader("User-Agent"));
    }

    /**
     * GET /api/roles-pago/0202125712                     -> el mas reciente
     * GET /api/roles-pago/0202125712?periodo=2026-08     -> uno concreto
     * GET /api/roles-pago/0202125712?descarga=true       -> audita DESCARGA_ROL
     */
    @GetMapping("/{cedula}")
    public RolPagoContenidoDto consultar(@PathVariable String cedula,
                                         @RequestParam(required = false) String periodo,
                                         @RequestParam(defaultValue = "false") boolean descarga,
                                         @AuthenticationPrincipal Usuario usuario,
                                         HttpServletRequest req) {
        return consultaService.consultar(cedula, periodo, descarga, usuario,
                req.getRemoteAddr(), req.getHeader("User-Agent"));
    }
}

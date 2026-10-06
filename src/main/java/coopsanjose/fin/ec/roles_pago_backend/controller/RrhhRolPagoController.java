package coopsanjose.fin.ec.roles_pago_backend.controller;

import coopsanjose.fin.ec.roles_pago_backend.dto.ArchivoResumenDto;
import coopsanjose.fin.ec.roles_pago_backend.dto.PublicacionDto;
import coopsanjose.fin.ec.roles_pago_backend.dto.ResultadoCargaDto;
import coopsanjose.fin.ec.roles_pago_backend.entity.Usuario;
import coopsanjose.fin.ec.roles_pago_backend.service.PeriodoService;
import coopsanjose.fin.ec.roles_pago_backend.service.RolPagoCargaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/rrhh/roles-pago")
@RequiredArgsConstructor
public class RrhhRolPagoController {

    private final RolPagoCargaService cargaService;
    // campo nuevo junto a cargaService:
    private final PeriodoService periodoService;

    // import ...dto.PublicacionDto; ...service.PeriodoService;
    @PostMapping("/periodos/{periodo}/publicar")
    public PublicacionDto publicar(@PathVariable String periodo,
                                   @AuthenticationPrincipal Usuario rrhh) {
        return periodoService.publicar(parsePeriodo(periodo), rrhh);
    }

    /** Carga masiva: form-data con "periodo" (yyyy-MM) y varios "archivos". */
    @PostMapping(value = "/carga", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoCargaDto cargar(@RequestParam("periodo") String periodo,
                                    @RequestParam("archivos") List<MultipartFile> archivos,
                                    @AuthenticationPrincipal Usuario rrhh) {
        if (archivos == null || archivos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe adjuntar al menos un archivo");
        }
        return cargaService.procesarLote(parsePeriodo(periodo), archivos, rrhh);
    }

    /** Lista lo cargado en un periodo (para que RRHH revise antes de publicar). */
    @GetMapping("/periodos/{periodo}")
    public List<ArchivoResumenDto> listar(@PathVariable String periodo) {
        return cargaService.listar(parsePeriodo(periodo));
    }

    private YearMonth parsePeriodo(String periodo) {
        try {
            return YearMonth.parse(periodo);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Periodo invalido. Formato esperado: yyyy-MM (ej. 2026-08)");
        }
    }
}
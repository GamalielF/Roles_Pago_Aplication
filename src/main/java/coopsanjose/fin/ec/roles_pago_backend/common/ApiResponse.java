package coopsanjose.fin.ec.roles_pago_backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String status,
        String message,
        T content,
        List<String> errors,
        LocalDateTime timestamp
) {

    // Constructor principal
    public ApiResponse {
        if (status == null) status = "500";
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    // Respuesta exitosa sin datos
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("200", message, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(String message) {
        return new ApiResponse<>("201", message, null, null, LocalDateTime.now());
    }

    // Respuesta exitosa con datos
    public static <T> ApiResponse<T> successWithData(String message, T content) {
        return new ApiResponse<>("200", message, content, null, LocalDateTime.now());
    }

    // Respuesta exitosa con datos y metacontent
    public static <T> ApiResponse<T> success(String message, T content, List<String> warnings) {
        return new ApiResponse<>("200", message, content, warnings, LocalDateTime.now());
    }

    // Respuesta de error simple
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("400", message, null, null, LocalDateTime.now());
    }

    // Respuesta de error con código específico
    public static <T> ApiResponse<T> error(String status, String message) {
        return new ApiResponse<>(status, message, null, null, LocalDateTime.now());
    }

    // Respuesta de error con lista de errores
    public static <T> ApiResponse<T> error(String status, String message, List<String> errors) {
        return new ApiResponse<>(status, message, null, errors, LocalDateTime.now());
    }

    // Respuesta de error con datos adicionales
    public static <T> ApiResponse<T> error(String status, String message, T content) {
        return new ApiResponse<>(status, message, content, null, LocalDateTime.now());
    }

    // Respuesta de validación (400 con detalles)
    public static <T> ApiResponse<T> validationError(List<String> errors) {
        return new ApiResponse<>("400", "Error de validación", null, errors, LocalDateTime.now());
    }

    // Respuesta no encontrada
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>("404", message, null, null, LocalDateTime.now());
    }

    // Respuesta no autorizada
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>("401", message, null, null, LocalDateTime.now());
    }

    // Respuesta prohibida
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>("403", message, null, null, LocalDateTime.now());
    }

    // Respuesta error interno del servidor
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>("500", message, null, null, LocalDateTime.now());
    }

    // Métodos de utilidad
    public boolean isSuccess() {
        return status != null && status.startsWith("2");
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}

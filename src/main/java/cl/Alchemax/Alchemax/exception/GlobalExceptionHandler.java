package cl.Alchemax.Alchemax.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResp> handleValidation(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResp error = new ErrorResp(
                "400",
                "Datos inválidos",
                detalle.isBlank() ? "Los datos enviados no son válidos." : detalle,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResp> handleSolicitudInvalida(SolicitudInvalidaException ex) {
        ErrorResp error = new ErrorResp(
                "400",
                "Solicitud inválida",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResp> handleAccesoDenegado(AccesoDenegadoException ex) {
        ErrorResp error = new ErrorResp(
                "403",
                "Acceso denegado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<ErrorResp> handleHorarioNoDisponible(HorarioNoDisponibleException ex) {
        ErrorResp error = new ErrorResp(
                "409",
                "Horario no disponible",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResp> handleDataIntegrity(DataIntegrityViolationException ex) {
        ErrorResp error = new ErrorResp(
                "409",
                "Conflicto de integridad",
                "La operación entra en conflicto con un registro existente.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(SesionExpiradaException.class)
    public ResponseEntity<ErrorResp> handleSesionExpirada(SesionExpiradaException ex) {
        ErrorResp error = new ErrorResp(
                "401",
                "Sesión expirada",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}

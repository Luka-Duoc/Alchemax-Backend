package cl.Alchemax.Alchemax.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    @Schema(description = "Código de estado HTTP")
    private String status;
    @Schema(description = "Tipo o categoría del error")
    private String error;
    @Schema(description = "Detalle del motivo del error")
    private String mensaje;
    @Schema(description = "Fecha y hora del error")
    private LocalDateTime fechaError;

}

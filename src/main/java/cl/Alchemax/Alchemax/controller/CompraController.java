package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.AccesoDenegadoException;
import cl.Alchemax.Alchemax.exception.ErrorResponse;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "Compras", description = "Controlador para el CRUD de compras")
@RequestMapping("/api/compras")
@CrossOrigin(origins = "http://localhost:5173")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Operation(
            summary = "Listar compras por usuario",
            description = "Se obtiene la lista de compras registradas para un usuario específico"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Compras del usuario obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Compra.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para consultar el historial de compras de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Compra>> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para consultar el historial de compras de otro usuario."
        );
        return ResponseEntity.ok(compraService.listarPorUsuario(usuarioId));
    }

    @Operation(
            summary = "Registrar compra",
            description = "Crea y guarda una nueva compra asociada al ID de un usuario"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Compra registrada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Compra.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Usuario no encontrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para registrar compras en la cuenta de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> guardar(
            @PathVariable Long usuarioId,
            @RequestBody Compra c,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para registrar compras en la cuenta de otro usuario."
        );
        Compra compra = compraService.guardar(usuarioId, c);
        if (compra == null) {
            ErrorResponse error = new ErrorResponse(
                    "400",
                    "Solicitud inválida",
                    "Usuario no encontrado.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }
        return new ResponseEntity<>(compra, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar compra",
            description = "Busca una compra por su ID y la elimina (restringido a ADMIN)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Compra eliminada con éxito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Solo un administrador puede eliminar registros de compras",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdmin(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo un administrador puede eliminar registros de compras."
            );
        }
        compraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

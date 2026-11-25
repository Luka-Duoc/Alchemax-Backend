package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.ErrorResp;
import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Tag(name = "Carrito", description = "Controlador para el CRUD de carrito")
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "http://localhost:5173")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Operation(
            summary = "Obtiene el carrito por Usuario",
            description = "Método que obtiene el carrito por id del usuario"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Carrito obtenido con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Carrito.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para acceder al carrito de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado para el usuario indicado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Carrito> obtenerCarrito(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para acceder al carrito de otro usuario."
        );
        Carrito carrito = carritoService.obtenerPorUsuario(usuarioId);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carrito);
    }

    @Operation(
            summary = "Agrega el item al carrito",
            description = "Agrega un servicio con una cantidad específica al carrito"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ítem agregado al carrito con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Carrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Usuario o Servicio no válido",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para modificar el carrito de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @PostMapping("/usuario/{usuarioId}/agregar")
    public ResponseEntity<?> agregarItem(
            @PathVariable Long usuarioId,
            @RequestParam Long servicioId,
            @RequestParam(defaultValue = "1") int cantidad,
            Authentication authentication) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para modificar el carrito de otro usuario."
        );

        Carrito carrito = carritoService.agregarItem(usuarioId, servicioId, cantidad);
        if (carrito == null) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Solicitud inválida",
                    "Usuario o Servicio no válido.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }
        return ResponseEntity.ok(carrito);
    }

    @Operation(
            summary = "Remover el item del carrito",
            description = "Elimina un item específico del carrito"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ítem removido del carrito con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Carrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "No se pudo remover el ítem",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para modificar el carrito de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @DeleteMapping("/usuario/{usuarioId}/remover")
    public ResponseEntity<?> removerItem(
            @PathVariable Long usuarioId,
            @RequestParam Long servicioId,
            Authentication authentication) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para modificar el carrito de otro usuario."
        );

        Carrito carrito = carritoService.removerItem(usuarioId, servicioId);
        if (carrito == null) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Solicitud inválida",
                    "No se pudo remover el ítem.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }
        return ResponseEntity.ok(carrito);
    }

    @Operation(
            summary = "Vaciar carrito",
            description = "Elimina todos los items contenidos en el carrito del usuario"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Carrito vaciado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Carrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Carrito no encontrado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para vaciar el carrito de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<?> vaciarCarrito(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para vaciar el carrito de otro usuario."
        );

        Carrito carrito = carritoService.vaciarCarrito(usuarioId);
        if (carrito == null) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Solicitud inválida",
                    "Carrito no encontrado.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }
        return ResponseEntity.ok(carrito);
    }
}

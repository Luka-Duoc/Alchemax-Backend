package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Carrito", description = "Controlador para el CRUD de carrito")
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "http://localhost:5173")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Operation(
            summary = "Obtiene el carrito por Usuario",
            description = "Metodo que obtiene el carrito por id del usuario"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Carrito encontrado",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Carrito.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado para el usuario especificado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerCarrito(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.obtenerPorUsuario(usuarioId);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carrito);
    }

    @Operation(
            summary = "Agrega el item al carrito",
            description = "Agrega un servicio con una cantidad especifica al carrito"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Item agregado con éxito, se devuelve el carrito actualizado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Carrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Usuario o Item no válido",
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
                    schema = @Schema(type = "object", implementation = ErrorResponse.class)
            )
    )
    @PostMapping("/usuario/{usuarioId}/agregar")
    public ResponseEntity<?> agregarItem(
            @PathVariable Long usuarioId,
            @RequestParam Long servicioId,
            @RequestParam(defaultValue = "1") int cantidad) {

        Carrito carrito = carritoService.agregarItem(usuarioId, servicioId, cantidad);
        if (carrito == null) {
            return ResponseEntity.badRequest().body("Usuario o Servicio no válido.");
        }
        return ResponseEntity.ok(carrito);
    }

    @Operation(
            summary = "Remover el item del carrito",
            description = "Elimina un item especifico dwl carrito"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Item removido con éxito, devuelve carrito actualizado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Carrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Hubo un error al remover el item",
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
                    schema = @Schema(type = "object", implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping("/usuario/{usuarioId}/remover")
    public ResponseEntity<?> removerItem(
            @PathVariable Long usuarioId,
            @RequestParam Long servicioId) {

        Carrito carrito = carritoService.removerItem(usuarioId, servicioId);
        if (carrito == null) {
            return ResponseEntity.badRequest().body("No se pudo remover el ítem.");
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<?> vaciarCarrito(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.vaciarCarrito(usuarioId);
        if (carrito == null) {
            return ResponseEntity.badRequest().body("Carrito no encontrado.");
        }
        return ResponseEntity.ok(carrito);
    }
}

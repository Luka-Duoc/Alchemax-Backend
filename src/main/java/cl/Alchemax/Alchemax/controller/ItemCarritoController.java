package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.ErrorResponse;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.service.ItemCarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "Items Carrito", description = "Controlador para el CRUD de ítems del carrito")
@RequestMapping("/api/items-carrito")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemCarritoController {

    @Autowired
    private ItemCarritoService itemCarritoService;

    @Operation(
            summary = "Listar todos los ítems de carrito",
            description = "Método que muestra una lista de todos los ítems registrados en los carritos"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ítems encontrados con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemCarrito.class)
                    )
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
    @GetMapping
    public ResponseEntity<List<ItemCarrito>> listar() {
        return ResponseEntity.ok(itemCarritoService.listar());
    }

    @Operation(
            summary = "Buscar ítem de carrito por ID",
            description = "Busca un ítem de carrito registrado mediante su ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ítem encontrado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemCarrito.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del ítem no se pudo encontrar",
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
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        ItemCarrito item = itemCarritoService.buscarPorId(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @Operation(
            summary = "Listar ítems por carrito",
            description = "Obtiene la lista de ítems asociados a un carrito específico"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ítems encontrados con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemCarrito.class)
                    )
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
    @GetMapping("/carrito/{carritoId}")
    public ResponseEntity<List<ItemCarrito>> listarPorCarrito(@PathVariable Long carritoId) {
        return ResponseEntity.ok(itemCarritoService.listarPorCarrito(carritoId));
    }

    @Operation(
            summary = "Crear ítem de carrito",
            description = "Agrega o incrementa un ítem asociado a un carrito y un servicio específico"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Ítem guardado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemCarrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Carrito o Servicio no válido",
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
    @PostMapping("/carrito/{carritoId}")
    public ResponseEntity<?> guardar(
            @PathVariable Long carritoId,
            @RequestParam Long servicioId,
            @RequestParam(defaultValue = "1") int cantidad) {

        ItemCarrito item = itemCarritoService.guardar(carritoId, servicioId, cantidad);
        if (item == null) {
            return ResponseEntity.badRequest().body("Carrito o Servicio no válido.");
        }
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar ítem de carrito",
            description = "Busca el ID del ítem de carrito y actualiza su cantidad o servicio asociado"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ítem actualizado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ItemCarrito.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Cantidad no válida",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del ítem o del servicio no se pudo encontrar",
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
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@RequestBody ItemCarrito item, @PathVariable Long id) {
        if (item.getCantidad() <= 0) {
            ErrorResponse error = new ErrorResponse(
                    "400",
                    "Cantidad no válida",
                    "La cantidad debe ser mayor a 0.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }

        ItemCarrito actualizado = itemCarritoService.actualizar(item, id);
        if (actualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizado);
    }

    @Operation(
            summary = "Eliminar ítem de carrito",
            description = "Busca un ítem de carrito por su ID y lo elimina"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Ítem eliminado con éxito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del ítem no se pudo encontrar",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!itemCarritoService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

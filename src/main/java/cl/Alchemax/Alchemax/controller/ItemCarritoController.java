package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.AccesoDenegadoException;
import cl.Alchemax.Alchemax.exception.ErrorResp;
import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.ItemCarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            description = "Método que muestra una lista de todos los ítems registrados en los carritos (restringido a ADMIN)"
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
            responseCode = "403",
            description = "Acceso denegado (solo ADMIN)",
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
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @GetMapping
    public ResponseEntity<List<ItemCarrito>> listar(Authentication authentication) {
        if (!AccessControlHelper.esAdmin(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo un administrador puede listar todos los ítems de carrito."
            );
        }
        return ResponseEntity.ok(itemCarritoService.listar());
    }

    @Operation(
            summary = "Buscar ítem de carrito por ID",
            description = "Busca un ítem de carrito registrado mediante su ID validando propiedad"
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
            responseCode = "403",
            description = "No tienes permisos para acceder a un ítem de otro carrito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del ítem no se pudo encontrar",
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
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        ItemCarrito item = itemCarritoService.buscarPorId(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        AccessControlHelper.verificarAccesoItemCarrito(
                authentication,
                item,
                "No tienes permisos para acceder a un ítem de otro carrito."
        );
        return ResponseEntity.ok(item);
    }

    @Operation(
            summary = "Listar ítems por carrito",
            description = "Obtiene la lista de ítems asociados a un carrito específico validando propiedad"
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
            responseCode = "403",
            description = "No tienes permisos para consultar los ítems del carrito de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado",
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
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @GetMapping("/carrito/{carritoId}")
    public ResponseEntity<List<ItemCarrito>> listarPorCarrito(
            @PathVariable Long carritoId,
            Authentication authentication
    ) {
        Carrito carrito = itemCarritoService.obtenerCarrito(carritoId);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        Long ownerId = (carrito.getUsuario() != null) ? carrito.getUsuario().getId() : null;
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                ownerId,
                "No tienes permisos para consultar los ítems del carrito de otro usuario."
        );
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
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @PostMapping("/carrito/{carritoId}")
    public ResponseEntity<?> guardar(
            @PathVariable Long carritoId,
            @RequestParam Long servicioId,
            @RequestParam(defaultValue = "1") int cantidad,
            Authentication authentication) {

        Carrito carrito = itemCarritoService.obtenerCarrito(carritoId);
        if (carrito == null) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Solicitud inválida",
                    "Carrito o Servicio no válido.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }

        Long ownerId = (carrito.getUsuario() != null) ? carrito.getUsuario().getId() : null;
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                ownerId,
                "No tienes permisos para modificar el carrito de otro usuario."
        );

        ItemCarrito item = itemCarritoService.guardarEnCarrito(carrito, servicioId, cantidad);
        if (item == null) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Solicitud inválida",
                    "Carrito o Servicio no válido.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para modificar un ítem de otro carrito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del ítem o del servicio no se pudo encontrar",
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
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @Valid @RequestBody ItemCarrito item,
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (item.getCantidad() <= 0) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Cantidad no válida",
                    "La cantidad debe ser mayor a 0.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }

        ItemCarrito existente = itemCarritoService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        AccessControlHelper.verificarAccesoItemCarrito(
                authentication,
                existente,
                "No tienes permisos para modificar un ítem de otro carrito."
        );

        ItemCarrito actualizado = itemCarritoService.actualizarExistente(existente, item);
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
            responseCode = "403",
            description = "No tienes permisos para eliminar un ítem de otro carrito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del ítem no se pudo encontrar",
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
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        ItemCarrito existente = itemCarritoService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        AccessControlHelper.verificarAccesoItemCarrito(
                authentication,
                existente,
                "No tienes permisos para eliminar un ítem de otro carrito."
        );

        if (!itemCarritoService.eliminarExistente(existente)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

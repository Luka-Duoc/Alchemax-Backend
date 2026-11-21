package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            description = "Método que obtiene el carrito por id del usuario"
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Carrito> obtenerCarrito(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoUsuario(
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
    @PostMapping("/usuario/{usuarioId}/agregar")
    public ResponseEntity<?> agregarItem(
            @PathVariable Long usuarioId,
            @RequestParam Long servicioId,
            @RequestParam(defaultValue = "1") int cantidad,
            Authentication authentication) {
        AccessControlHelper.verificarAccesoUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para modificar el carrito de otro usuario."
        );

        Carrito carrito = carritoService.agregarItem(usuarioId, servicioId, cantidad);
        if (carrito == null) {
            return ResponseEntity.badRequest().body("Usuario o Servicio no válido.");
        }
        return ResponseEntity.ok(carrito);
    }

    @Operation(
            summary = "Remover el item del carrito",
            description = "Elimina un item específico del carrito"
    )
    @DeleteMapping("/usuario/{usuarioId}/remover")
    public ResponseEntity<?> removerItem(
            @PathVariable Long usuarioId,
            @RequestParam Long servicioId,
            Authentication authentication) {
        AccessControlHelper.verificarAccesoUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para modificar el carrito de otro usuario."
        );

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
    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<?> vaciarCarrito(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para vaciar el carrito de otro usuario."
        );

        Carrito carrito = carritoService.vaciarCarrito(usuarioId);
        if (carrito == null) {
            return ResponseEntity.badRequest().body("Carrito no encontrado.");
        }
        return ResponseEntity.ok(carrito);
    }
}

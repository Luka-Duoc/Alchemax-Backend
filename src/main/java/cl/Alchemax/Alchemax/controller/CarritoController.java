package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "http://localhost:5173")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerCarrito(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.obtenerPorUsuario(usuarioId);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carrito);
    }

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

    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<?> vaciarCarrito(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.vaciarCarrito(usuarioId);
        if (carrito == null) {
            return ResponseEntity.badRequest().body("Carrito no encontrado.");
        }
        return ResponseEntity.ok(carrito);
    }
}

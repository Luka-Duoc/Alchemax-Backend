package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Compra>> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoUsuario(
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
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> guardar(
            @PathVariable Long usuarioId,
            @RequestBody Compra c,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para registrar compras en la cuenta de otro usuario."
        );
        Compra compra = compraService.guardar(usuarioId, c);
        if (compra == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }
        return new ResponseEntity<>(compra, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar compra",
            description = "Busca una compra por su ID y la elimina"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        compraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

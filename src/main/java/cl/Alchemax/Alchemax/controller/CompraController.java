package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin(origins = "http://localhost:5173")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Compra>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(compraService.listarPorUsuario(usuarioId));
    }

    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> guardar(@PathVariable Long usuarioId, @RequestBody Compra c) {
        Compra compra = compraService.guardar(usuarioId, c);
        if (compra == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }
        return new ResponseEntity<>(compra, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        compraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

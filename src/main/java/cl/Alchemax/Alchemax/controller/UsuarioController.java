package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173/Alchemax/")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        List<Usuario> u = usuarioService.listar();
        return ResponseEntity.ok(u);
    }

    @PostMapping
    public ResponseEntity<Usuario> guardar(@RequestBody Usuario u) {
        Usuario user = usuarioService.guardar(u);
        if(user == null) return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@RequestBody Usuario u, @PathVariable Long id) {
        Usuario user = usuarioService.editar(u, id);
        if(user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }
}

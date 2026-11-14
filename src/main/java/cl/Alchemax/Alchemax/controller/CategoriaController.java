package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Categoria;
import cl.Alchemax.Alchemax.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<?>> listar() {
        List<Categoria> c = categoriaService.listar();
        return ResponseEntity.ok(c);
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Categoria c) {
        Categoria cat = categoriaService.guardar(c);
        if (cat == null) return new ResponseEntity<>(cat,HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(cat, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            categoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se puede eliminar la categoría porque tiene servicios asociados.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@RequestBody Categoria c, @PathVariable Long id) {
        Categoria cat = categoriaService.actualizar(c, id);
        if (cat == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cat);
    }
}

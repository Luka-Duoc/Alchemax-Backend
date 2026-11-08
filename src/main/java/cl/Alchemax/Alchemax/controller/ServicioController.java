package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.service.ServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "http://localhost:5173")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @GetMapping
    public ResponseEntity<List<?>> listar() {
        return ResponseEntity.ok(servicioService.listar());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<?>> buscarPorNombre(@RequestParam String nombre) {
        List<Servicio> servicio = servicioService.buscarServicio(nombre);
        if (servicio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(servicio);
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Servicio s) {
        Servicio ser = servicioService.guardar(s);
        if (ser == null) return new ResponseEntity<>(ser, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(ser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@RequestBody Servicio s,@PathVariable Long id) {
        Servicio ser = servicioService.actualizar(s, id);
        if(ser == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ser);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id) {
        Servicio servicioActualizado = servicioService.cambiarEstado(id);
        if (servicioActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(servicioActualizado);
    }
}

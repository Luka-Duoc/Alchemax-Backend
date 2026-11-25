package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.ErrorResp;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.service.ServicioService;
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

import java.util.List;

@RestController
@Tag(name = "Servicios", description= "Controlador para el CRUD de Servicios")
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "http://localhost:5173")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Operation(
            summary = "Listar todos los servicios",
            description = "Método que muestra una lista de todos los servicios registrados"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Servicios encontrados con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Servicio.class)
                    )
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
    public ResponseEntity<List<?>> listar() {
        return ResponseEntity.ok(servicioService.listar());
    }

    @Operation(
            summary = "Buscar servicios por nombre",
            description = "Busca servicios registrados que coincidan con el nombre indicado"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Servicios encontrados con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Servicio.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No se encontraron servicios con ese nombre",
            content = @Content
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @GetMapping("/buscar")
    public ResponseEntity<List<?>> buscarPorNombre(@RequestParam String nombre) {
        List<Servicio> servicio = servicioService.buscarServicio(nombre);
        if (servicio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(servicio);
    }

    @Operation(
            summary = "Crear servicio",
            description = "Guarda un nuevo servicio en la base de datos"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Servicio guardado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Servicio.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "El servicio no pudo ser creado o datos no validos",
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
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Servicio s) {
        Servicio ser = servicioService.guardar(s);
        if (ser == null) return new ResponseEntity<>(ser, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(ser, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Editar servicio",
            description = "Busca el ID del servicio y actualiza su información correspondiente"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Servicio actualizado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Servicio.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del servicio no se pudo encontrar",
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
    public ResponseEntity<?> editar(@RequestBody Servicio s,@PathVariable Long id) {
        Servicio ser = servicioService.actualizar(s, id);
        if(ser == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ser);
    }

    @Operation(
            summary = "Cambiar estado del servicio",
            description = "Alterna o actualiza el estado del servicio identificado por su ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Estado del servicio actualizado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Servicio.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del servicio no se pudo encontrar",
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
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id) {
        Servicio servicioActualizado = servicioService.cambiarEstado(id);
        if (servicioActualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(servicioActualizado);
    }

    @Operation(
            summary = "Eliminar servicio",
            description = "Busca un servicio por su ID y lo elimina"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!servicioService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

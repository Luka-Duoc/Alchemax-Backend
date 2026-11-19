package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Categoria;
import cl.Alchemax.Alchemax.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import javax.smartcardio.CardTerminal;
import java.util.List;

@RestController
@Tag(name = "Categorias", description = "Controlador para el CRUD de categorías")
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Operation(
            summary = "Listar las categorías",
            description = "Metodo que lista todas las categorías registradas"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categoría encontradas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categoria.class)
                    )
            )
    @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontradas",
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
    @GetMapping
    public ResponseEntity<List<?>> listar() {
        List<Categoria> c = categoriaService.listar();
        return ResponseEntity.ok(c);
    }

    @Operation(
            summary = "Crear categoria",
            description = "Se guarda una nueva categoría en la base de datos"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Categoría guardada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categoria.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "La categoría no pudo ser creada",
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
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Categoria c) {
        Categoria cat = categoriaService.guardar(c);
        if (cat == null) return new ResponseEntity<>(cat,HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(cat, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar categoría",
            description = "Busca una categoría por ID y la elimina"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Categoriía eliminada con éxito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "No se puede eliminar la categoría debido a servicios asociados a ella",
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
        try {
            categoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se puede eliminar la categoría porque tiene servicios asociados.");
        }
    }

    @Operation(
            summary = "Actualizar Categoría",
            description = "Busca el ID de la categoría y actualiza su información"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categoría actualizada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Categoria.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID de la categoría no se pudo encontrar",
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
    public ResponseEntity<?> actualizar(@RequestBody Categoria c, @PathVariable Long id) {
        Categoria cat = categoriaService.actualizar(c, id);
        if (cat == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cat);
    }
}

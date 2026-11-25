package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.service.UsuarioService;
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
import org.springframework.web.bind.annotation.*;
import cl.Alchemax.Alchemax.exception.ErrorResp;

import java.util.List;

@RestController
@Tag(name = "Usuarios", description = "Controlador para el CRUD de usuarios")
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(
            summary = "Listar a todos los usuarios",
            description = "Metodo que muestra una lista de todos los usuarios registrados"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Usuarios encontrados",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Usuario.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuarios no encontrados",
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
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        List<Usuario> u = usuarioService.listar();
        return ResponseEntity.ok(u);
    }

    @Operation(
            summary = "Busca al usuario por ID",
            description = "Busca al usuario registrado por ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado con exito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Usuario.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Id de usuario no encontrado",
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
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Crear usuario",
            description = "Guarda un usuario en la BD"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Usuario guardado con exito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Usuario.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuario no pudo ser creado",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @PostMapping
    public ResponseEntity<Usuario> guardar(@Valid @RequestBody Usuario u) {
        Usuario user = usuarioService.guardar(u);
        if(user == null) return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


    @Operation(
            summary = "Eliminar Usuario",
            description = "Busca un usuario por ID y lo elimina"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Usuario eliminado con exito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del usuario no se pudo encontrar",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error interno en el servidor",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object", implementation = ErrorResp.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Actualizar Usuario",
            description = "Busca el ID del usuario y actualiza la información del usuario correspondiente"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Usuario Actualizado con exito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Usuario.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID del usuario no se pudo encontrar",
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
    public ResponseEntity<?> actualizar(@RequestBody Usuario u, @PathVariable Long id) {
        Usuario user = usuarioService.editar(u, id);
        if(user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }
}

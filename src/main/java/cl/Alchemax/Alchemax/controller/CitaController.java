package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.ErrorResponse;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.CitaService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@Tag(name = "Citas", description = "Controlador para el CRUD de citas")
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:5173")
public class CitaController {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "Programada",
            "Confirmada",
            "Completada",
            "Cancelada"
    );

    @Autowired
    private CitaService citaService;

    @Operation(
            summary = "Listar todas las citas",
            description = "Método que muestra una lista de todas las citas registradas"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas encontradas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Cita.class)
                    )
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
    public ResponseEntity<List<Cita>> listar() {
        return ResponseEntity.ok(citaService.listar());
    }

    @Operation(
            summary = "Buscar cita por ID",
            description = "Busca una cita registrada mediante su ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Cita encontrada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Cita.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID de la cita no se pudo encontrar",
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
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Cita cita = citaService.buscarPorId(id);
        if (cita == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cita);
    }

    @Operation(
            summary = "Listar citas por estado",
            description = "Obtiene todas las citas filtradas por estado (Programada, Completada o Cancelada)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas encontradas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Cita.class)
                    )
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
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Cita>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(citaService.listarPorEstado(estado));
    }

    @Operation(
            summary = "Listar citas por profesional",
            description = "Obtiene todas las citas asignadas a un profesional específico"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas encontradas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Cita.class)
                    )
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
    @GetMapping("/profesional")
    public ResponseEntity<List<Cita>> listarPorProfesional(@RequestParam String profesional) {
        return ResponseEntity.ok(citaService.listarPorProfesional(profesional));
    }

    @Operation(
            summary = "Listar citas por fecha",
            description = "Obtiene todas las citas programadas para una fecha específica"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas encontradas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Cita.class)
                    )
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
    @GetMapping("/fecha")
    public ResponseEntity<List<Cita>> listarPorFecha(@RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.listarPorFecha(fecha));
    }

    @Operation(
            summary = "Listar citas por usuario",
            description = "Obtiene todas las citas asociadas a un paciente por su ID de usuario"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas del usuario encontradas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = Cita.class)
                    )
            )
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        if (!AccessControlHelper.puedeAccederRecursoUsuario(authentication, usuarioId)) {
            ErrorResponse error = new ErrorResponse(
                    "403",
                    "Acceso denegado",
                    "No tienes permisos para consultar las citas de otro usuario.",
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        return ResponseEntity.ok(citaService.listarPorUsuario(usuarioId));
    }

    @Operation(
            summary = "Crear cita",
            description = "Guarda una nueva cita en la base de datos"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cita guardada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Cita.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "La cita no pudo ser creada o datos no válidos",
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
    public ResponseEntity<?> guardar(@Valid @RequestBody Cita c, Authentication authentication) {
        if (c.getUsuario() != null
                && c.getUsuario().getId() != null
                && !AccessControlHelper.puedeAccederRecursoUsuario(authentication, c.getUsuario().getId())) {
            ErrorResponse error = new ErrorResponse(
                    "403",
                    "Acceso denegado",
                    "No puedes agendar citas a nombre de otro usuario.",
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        if (citaService.tieneConflictoHorario(c.getProfesional(), c.getFecha(), c.getHora(), null)) {
            ErrorResponse error = new ErrorResponse(
                    "409",
                    "Horario no disponible",
                    "El profesional seleccionado ya cuenta con una cita activa en esa fecha y hora.",
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        Cita cita = citaService.guardar(c);
        if (cita == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(cita, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar cita",
            description = "Busca el ID de la cita y actualiza su información correspondiente"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Cita actualizada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Cita.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de la cita no válidos",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID de la cita no se pudo encontrar",
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
    public ResponseEntity<?> actualizar(@Valid @RequestBody Cita c, @PathVariable Long id) {
        if (citaService.tieneConflictoHorario(c.getProfesional(), c.getFecha(), c.getHora(), id)) {
            ErrorResponse error = new ErrorResponse(
                    "409",
                    "Horario no disponible",
                    "El profesional seleccionado ya cuenta con una cita activa en esa fecha y hora.",
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        Cita cita = citaService.actualizar(c, id);
        if (cita == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cita);
    }

    @Operation(
            summary = "Cambiar estado de la cita",
            description = "Actualiza el estado de la cita identificada por su ID (Programada, Completada o Cancelada)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Estado de la cita actualizado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Cita.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Estado no válido",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID de la cita no se pudo encontrar",
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
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        if (!ESTADOS_VALIDOS.contains(estado)) {
            ErrorResponse error = new ErrorResponse(
                    "400",
                    "Estado no válido",
                    "El estado debe ser Programada, Confirmada, Completada o Cancelada.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }

        Cita citaActualizada = citaService.cambiarEstado(id, estado);
        if (citaActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(citaActualizada);
    }

    @Operation(
            summary = "Eliminar cita",
            description = "Busca una cita por su ID y la elimina"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Cita eliminada con éxito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "El ID de la cita no se pudo encontrar",
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
        if (!citaService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

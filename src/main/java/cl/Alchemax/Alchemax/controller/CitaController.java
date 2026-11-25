package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.AccesoDenegadoException;
import cl.Alchemax.Alchemax.exception.ErrorResp;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Usuario;
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

@RestController
@Tag(name = "Citas", description = "Controlador para el CRUD de citas")
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:5173")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Operation(
            summary = "Listar todas las citas",
            description = "Método que muestra una lista de todas las citas registradas (solo ADMIN o NUTRICIONISTA)"
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
            responseCode = "403",
            description = "Acceso denegado (requiere rol ADMIN o NUTRICIONISTA)",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @GetMapping
    public ResponseEntity<List<Cita>> listar() {
        return ResponseEntity.ok(citaService.listar());
    }

    @Operation(
            summary = "Buscar cita por ID",
            description = "Busca una cita registrada mediante su ID validando propiedad o rol clínico"
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
            responseCode = "403",
            description = "No tienes permisos para consultar esta cita",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Cita cita = citaService.buscarPorId(id);
        if (cita == null) {
            return ResponseEntity.notFound().build();
        }
        AccessControlHelper.verificarAccesoCita(
                authentication,
                cita,
                "No tienes permisos para consultar esta cita."
        );
        return ResponseEntity.ok(cita);
    }

    @Operation(
            summary = "Listar citas por estado",
            description = "Obtiene todas las citas filtradas por estado (solo ADMIN o NUTRICIONISTA)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas filtradas por estado obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Cita.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Cita>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(citaService.listarPorEstado(estado));
    }

    @Operation(
            summary = "Listar citas por profesional",
            description = "Obtiene todas las citas asignadas a un profesional específico (solo ADMIN o NUTRICIONISTA)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas filtradas por profesional obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Cita.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @GetMapping("/profesional")
    public ResponseEntity<List<Cita>> listarPorProfesional(@RequestParam String profesional) {
        return ResponseEntity.ok(citaService.listarPorProfesional(profesional));
    }

    @Operation(
            summary = "Listar citas por fecha",
            description = "Obtiene todas las citas programadas para una fecha específica (solo ADMIN o NUTRICIONISTA)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Citas filtradas por fecha obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Cita.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado",
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
                    schema = @Schema(implementation = ErrorResp.class)
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
            description = "Citas del usuario obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Cita.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para consultar las citas de otro usuario",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Cita>> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para consultar las citas de otro usuario."
        );
        return ResponseEntity.ok(citaService.listarPorUsuario(usuarioId));
    }

    @Operation(
            summary = "Crear cita",
            description = "Guarda una nueva cita en la base de datos validando propiedad y disponibilidad horaria"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Cita creada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Cita.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de la cita inválidos",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No puedes agendar citas a nombre de otro usuario",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Conflicto de horario para el profesional seleccionado",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @PostMapping
    public ResponseEntity<Cita> guardar(
            @Valid @RequestBody Cita c,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdminONutricionista(authentication)) {
            Long tokenUserId = AccessControlHelper.obtenerUsuarioIdDelToken(authentication)
                    .orElseThrow(() -> new AccesoDenegadoException(
                            "El token no contiene un identificador de usuario válido."
                    ));

            if (c.getUsuario() != null
                    && c.getUsuario().getId() != null
                    && !c.getUsuario().getId().equals(tokenUserId)) {
                throw new AccesoDenegadoException(
                        "No puedes agendar citas a nombre de otro usuario."
                );
            }

            // Vincula estrictamente la cita al usuario del token y fuerza estado inicial Programada
            Usuario propietario = new Usuario();
            propietario.setId(tokenUserId);
            c.setUsuario(propietario);
            c.setPacienteEmail(null);
            c.setPacienteNombre(null);
            c.setEstado(Cita.ESTADO_PROGRAMADA);
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
            description = "Datos de la cita inválidos",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para modificar esta cita",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Conflicto de horario para el profesional seleccionado",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizar(
            @Valid @RequestBody Cita c,
            @PathVariable Long id,
            Authentication authentication
    ) {
        Cita existente = citaService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        AccessControlHelper.verificarAccesoCita(
                authentication,
                existente,
                "No tienes permisos para modificar esta cita."
        );

        if (!AccessControlHelper.esAdminONutricionista(authentication)) {
            c.setUsuario(existente.getUsuario());
            c.setPacienteEmail(existente.getPacienteEmail());
            c.setPacienteNombre(existente.getPacienteNombre());
            // Un paciente no puede autoconfirmar ni completar una cita vía PUT general
            c.setEstado(existente.getEstado());
        }

        Cita cita = citaService.actualizarExistente(existente, c);
        if (cita == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cita);
    }

    @Operation(
            summary = "Cambiar estado de la cita",
            description = "Actualiza el estado de la cita identificada por su ID (Programada, Confirmada, Completada o Cancelada)"
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para cambiar el estado de esta cita",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Conflicto de horario al reactivar la cita",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            Authentication authentication
    ) {
        if (!Cita.ESTADOS_VALIDOS.contains(estado)) {
            ErrorResp error = new ErrorResp(
                    "400",
                    "Estado no válido",
                    "El estado debe ser Programada, Confirmada, Completada o Cancelada.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }

        Cita existente = citaService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        AccessControlHelper.verificarAccesoCita(
                authentication,
                existente,
                "No tienes permisos para cambiar el estado de esta cita."
        );

        if (!AccessControlHelper.esAdminONutricionista(authentication)
                && !Cita.ESTADO_CANCELADA.equals(estado)) {
            throw new AccesoDenegadoException(
                    "Un paciente solo puede cancelar sus propias citas."
            );
        }

        Cita citaActualizada = citaService.cambiarEstadoExistente(existente, estado);
        if (citaActualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(citaActualizada);
    }

    @Operation(
            summary = "Eliminar cita",
            description = "Busca una cita por su ID y la elimina (restringido a personal clínico o administrativo)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Cita eliminada con éxito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Solo el personal clínico o administrativo puede eliminar citas",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cita no encontrada",
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
                    schema = @Schema(implementation = ErrorResp.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdminONutricionista(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo el personal clínico o administrativo puede eliminar citas."
            );
        }

        Cita existente = citaService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        if (!citaService.eliminarExistente(existente)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

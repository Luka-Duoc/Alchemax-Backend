package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.AccesoDenegadoException;
import cl.Alchemax.Alchemax.exception.ErrorResponse;
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
    @GetMapping
    public ResponseEntity<List<Cita>> listar() {
        return ResponseEntity.ok(citaService.listar());
    }

    @Operation(
            summary = "Buscar cita por ID",
            description = "Busca una cita registrada mediante su ID"
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
            description = "Obtiene todas las citas filtradas por estado"
    )
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Cita>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(citaService.listarPorEstado(estado));
    }

    @Operation(
            summary = "Listar citas por profesional",
            description = "Obtiene todas las citas asignadas a un profesional específico"
    )
    @GetMapping("/profesional")
    public ResponseEntity<List<Cita>> listarPorProfesional(@RequestParam String profesional) {
        return ResponseEntity.ok(citaService.listarPorProfesional(profesional));
    }

    @Operation(
            summary = "Listar citas por fecha",
            description = "Obtiene todas las citas programadas para una fecha específica"
    )
    @GetMapping("/fecha")
    public ResponseEntity<List<Cita>> listarPorFecha(@RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.listarPorFecha(fecha));
    }

    @Operation(
            summary = "Listar citas por usuario",
            description = "Obtiene todas las citas asociadas a un paciente por su ID de usuario"
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

            // Vincula estrictamente la cita al usuario del token, ignorando pacienteEmail externo
            Usuario propietario = new Usuario();
            propietario.setId(tokenUserId);
            c.setUsuario(propietario);
            c.setPacienteEmail(null);
            c.setPacienteNombre(null);
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
        }

        Cita cita = citaService.actualizar(c, id);
        if (cita == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cita);
    }

    @Operation(
            summary = "Cambiar estado de la cita",
            description = "Actualiza el estado de la cita identificada por su ID (Programada, Confirmada, Completada o Cancelada)"
    )
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            Authentication authentication
    ) {
        if (!ESTADOS_VALIDOS.contains(estado)) {
            ErrorResponse error = new ErrorResponse(
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
                && !"Cancelada".equals(estado)) {
            throw new AccesoDenegadoException(
                    "Un paciente solo puede cancelar sus propias citas."
            );
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
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
                "No tienes permisos para eliminar esta cita."
        );

        if (!citaService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

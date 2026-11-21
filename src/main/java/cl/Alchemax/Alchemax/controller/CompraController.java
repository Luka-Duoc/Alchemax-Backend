package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.dto.ReporteAdminResponse;
import cl.Alchemax.Alchemax.exception.AccesoDenegadoException;
import cl.Alchemax.Alchemax.exception.ErrorResponse;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.security.AccessControlHelper;
import cl.Alchemax.Alchemax.service.CompraService;
import cl.Alchemax.Alchemax.service.ReporteAdminService;
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
import java.util.List;

@RestController
@Tag(name = "Compras", description = "Controlador para el CRUD de compras y reportes administrativos")
@RequestMapping("/api/compras")
@CrossOrigin(origins = "http://localhost:5173")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private ReporteAdminService reporteAdminService;

    @Operation(
            summary = "Listar todas las compras",
            description = "Obtiene la lista de compras registradas en el sistema con soporte opcional de paginación (solo ADMIN o NUTRICIONISTA)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Compras obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Compra.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado (requiere rol ADMIN o NUTRICIONISTA)",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping
    public ResponseEntity<List<Compra>> listarTodas(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdminONutricionista(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo el personal administrativo o clínico puede consultar todas las compras."
            );
        }
        if (page != null && size != null) {
            return ResponseEntity.ok(compraService.listarTodasPaginado(page, size));
        }
        return ResponseEntity.ok(compraService.listarTodas());
    }

    @Operation(
            summary = "Obtener reporte administrativo de operaciones y ventas",
            description = "Calcula indicadores reales desde la base de datos filtrados por período (semana, mes, anio o historico)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Reporte administrativo generado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReporteAdminResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Período de consulta no válido",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado (requiere rol ADMIN o NUTRICIONISTA)",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/reporte")
    public ResponseEntity<ReporteAdminResponse> obtenerReporte(
            @RequestParam(defaultValue = "historico") String periodo,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdminONutricionista(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo el personal administrativo o clínico puede consultar reportes globales."
            );
        }
        return ResponseEntity.ok(reporteAdminService.generarReporte(periodo));
    }

    @Operation(
            summary = "Buscar compra por ID",
            description = "Obtiene el detalle de una compra específica validando propiedad o rol administrativo"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Compra encontrada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Compra.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para consultar esta compra",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Compra no encontrada",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<Compra> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Compra compra = compraService.buscarPorId(id);
        if (compra == null) {
            return ResponseEntity.notFound().build();
        }
        Long ownerId = (compra.getUsuario() != null) ? compra.getUsuario().getId() : null;
        AccessControlHelper.verificarAccesoUsuario(
                authentication,
                ownerId,
                "No tienes permisos para consultar esta compra."
        );
        return ResponseEntity.ok(compra);
    }

    @Operation(
            summary = "Listar compras por usuario",
            description = "Se obtiene la lista de compras registradas para un usuario específico"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Compras del usuario obtenidas con éxito",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Compra.class))
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para consultar el historial de compras de otro usuario",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Compra>> listarPorUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para consultar el historial de compras de otro usuario."
        );
        return ResponseEntity.ok(compraService.listarPorUsuario(usuarioId));
    }

    @Operation(
            summary = "Registrar compra",
            description = "Crea y guarda una nueva compra asociada al ID de un usuario validando el servicio en el catálogo"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Compra registrada con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Compra.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Usuario o servicio no encontrado, o datos de compra inválidos",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "No tienes permisos para registrar compras en la cuenta de otro usuario",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<Compra> guardar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody Compra c,
            Authentication authentication
    ) {
        AccessControlHelper.verificarAccesoComercialUsuario(
                authentication,
                usuarioId,
                "No tienes permisos para registrar compras en la cuenta de otro usuario."
        );

        if (!AccessControlHelper.esAdmin(authentication)) {
            c.setEstado(Compra.ESTADO_ACTIVO);
            c.setFechaCompra(LocalDate.now());
        }

        Compra compra = compraService.guardar(usuarioId, c);
        return new ResponseEntity<>(compra, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar estado de una compra",
            description = "Actualiza el estado de una compra (Activo, Completado o Cancelado) — restringido a ADMIN o NUTRICIONISTA"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Estado de compra actualizado con éxito",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Compra.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Estado de compra no válido",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado (requiere rol ADMIN o NUTRICIONISTA)",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Compra no encontrada",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PutMapping("/{id}/estado")
    public ResponseEntity<Compra> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdminONutricionista(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo el personal administrativo o clínico puede actualizar el estado de una compra."
            );
        }

        Compra existente = compraService.buscarPorId(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        Compra actualizada = compraService.cambiarEstadoExistente(existente, estado);
        return ResponseEntity.ok(actualizada);
    }

    @Operation(
            summary = "Eliminar compra",
            description = "Busca una compra por su ID y la elimina (restringido a ADMIN)"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Compra eliminada con éxito",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "Solo un administrador puede eliminar registros de compras",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Compra no encontrada",
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
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (!AccessControlHelper.esAdmin(authentication)) {
            throw new AccesoDenegadoException(
                    "Solo un administrador puede eliminar registros de compras."
            );
        }
        if (!compraService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

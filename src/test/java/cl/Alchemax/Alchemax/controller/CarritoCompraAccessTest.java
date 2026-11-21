package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.dto.ReporteAdminResponse;
import cl.Alchemax.Alchemax.dto.ServicioSolicitadoDTO;
import cl.Alchemax.Alchemax.exception.GlobalExceptionHandler;
import cl.Alchemax.Alchemax.exception.SolicitudInvalidaException;
import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.CarritoService;
import cl.Alchemax.Alchemax.service.CompraService;
import cl.Alchemax.Alchemax.service.ReporteAdminService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CarritoController.class, CompraController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CarritoCompraAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @MockitoBean
    private CompraService compraService;

    @MockitoBean
    private ReporteAdminService reporteAdminService;

    @Test
    void carritoDebeRechazarAccesoDeOtroUsuarioOSinClaimUsuarioId() throws Exception {
        Carrito carrito = new Carrito();
        carrito.setId(1L);
        when(carritoService.obtenerPorUsuario(2L)).thenReturn(carrito);

        // Sin claim usuarioId -> 403 ErrorResponse JSON
        mockMvc.perform(get("/api/carrito/usuario/2").with(jwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("403"));

        // Otro paciente (usuarioId=99) -> 403
        mockMvc.perform(get("/api/carrito/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 99L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("403"));

        // Nutricionista no tiene acceso al carrito comercial de un paciente -> 403
        mockMvc.perform(get("/api/carrito/usuario/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("403"));

        // Propietario (usuarioId=2) -> 200
        mockMvc.perform(get("/api/carrito/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void comprasDebeRechazarAccesoDeOtroUsuarioOcultarFotoPerfilYValidarDelete() throws Exception {
        Usuario propietario = new Usuario();
        propietario.setId(2L);
        propietario.setNombre("Camila Soto");
        propietario.setEmail("camila@gmail.com");
        propietario.setFotoPerfil("data:image/png;base64,IMAGEN_PESADA_QUE_NO_DEBE_SERIALIZARSE");

        Compra compra = new Compra(
                5L,
                2,
                "Plan Deportivo",
                LocalDate.now(),
                120000,
                Compra.ESTADO_ACTIVO,
                propietario
        );

        when(compraService.listarPorUsuario(2L)).thenReturn(List.of(compra));
        when(compraService.buscarPorId(5L)).thenReturn(compra);
        when(compraService.buscarPorId(99L)).thenReturn(null);
        when(compraService.eliminar(5L)).thenReturn(true);
        when(compraService.eliminar(99L)).thenReturn(false);

        mockMvc.perform(get("/api/compras/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 99L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("403"));

        mockMvc.perform(get("/api/compras/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].usuario.fotoPerfil").doesNotExist());

        // Obtener por ID: propietario -> 200 sin fotoPerfil, otro usuario -> 403, inexistente -> 404
        mockMvc.perform(get("/api/compras/5")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Plan Deportivo"))
                .andExpect(jsonPath("$.usuario.fotoPerfil").doesNotExist());

        mockMvc.perform(get("/api/compras/5")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 88L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/compras/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNotFound());

        // Un paciente no puede eliminar registros de compras -> 403
        mockMvc.perform(delete("/api/compras/5")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        // Un administrador sí puede eliminar registros de compras -> 204 si existe, 404 si no existe
        mockMvc.perform(delete("/api/compras/5")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/compras/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardarCompraPorPacienteDebeValidarPayloadYForzarEstadoActivoYFechaActual() throws Exception {
        Usuario propietario = new Usuario();
        propietario.setId(2L);

        Compra guardada = new Compra(
                10L,
                2,
                "Plan Nutricional Deportivo",
                LocalDate.now(),
                120000,
                Compra.ESTADO_ACTIVO,
                propietario
        );

        when(compraService.guardar(eq(2L), any(Compra.class))).thenReturn(guardada);

        // Payload inválido (servicio <= 0) -> 400 Bad Request
        String bodyInvalido = """
                {
                  "servicio": 0,
                  "nombre": "",
                  "monto": -100
                }
                """;

        mockMvc.perform(post("/api/compras/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"));

        // Paciente intentando forzar estado "Completado" y fecha pasada -> controlador fuerza Activo y LocalDate.now()
        String bodyConManipulacion = """
                {
                  "servicio": 2,
                  "nombre": "Plan Falsificado",
                  "fechaCompra": "2020-01-01",
                  "monto": 1,
                  "estado": "Completado"
                }
                """;

        mockMvc.perform(post("/api/compras/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyConManipulacion))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.monto").value(120000));

        ArgumentCaptor<Compra> captor = ArgumentCaptor.forClass(Compra.class);
        verify(compraService).guardar(eq(2L), captor.capture());
        assertEquals(Compra.ESTADO_ACTIVO, captor.getValue().getEstado());
        assertEquals(LocalDate.now(), captor.getValue().getFechaCompra());
    }

    @Test
    void endpointsAdministrativosDeComprasYReportesDebenValidarRolYParametros() throws Exception {
        Usuario propietario = new Usuario();
        propietario.setId(2L);

        Compra compra = new Compra(
                5L,
                2,
                "Plan Nutricional Deportivo",
                LocalDate.of(2026, 11, 20),
                120000,
                Compra.ESTADO_ACTIVO,
                propietario
        );
        Compra compraCompletada = new Compra(
                5L,
                2,
                "Plan Nutricional Deportivo",
                LocalDate.of(2026, 11, 20),
                120000,
                Compra.ESTADO_COMPLETADO,
                propietario
        );

        ReporteAdminResponse reporte = new ReporteAdminResponse(
                "mes",
                LocalDate.of(2026, 10, 23),
                LocalDate.of(2026, 11, 21),
                4L,
                2L,
                1L,
                1L,
                0L,
                155000L,
                77500L,
                2L,
                1L,
                0L,
                1L,
                0L,
                2L,
                List.of(new ServicioSolicitadoDTO(2, "Plan Nutricional Deportivo", 1L, 120000L, 50.0))
        );

        when(compraService.listarTodas()).thenReturn(List.of(compra));
        when(compraService.listarTodasPaginado(0, 10)).thenReturn(List.of(compra));
        when(reporteAdminService.generarReporte("mes")).thenReturn(reporte);
        when(reporteAdminService.generarReporte("trimestre"))
                .thenThrow(new SolicitudInvalidaException("El período debe ser semana, mes, anio o historico."));
        when(compraService.buscarPorId(5L)).thenReturn(compra);
        when(compraService.buscarPorId(99L)).thenReturn(null);
        when(compraService.cambiarEstadoExistente(compra, Compra.ESTADO_COMPLETADO)).thenReturn(compraCompletada);
        when(compraService.cambiarEstadoExistente(compra, "Pendiente"))
                .thenThrow(new SolicitudInvalidaException("El estado debe ser Activo, Completado o Cancelado."));

        // Paciente no puede listar todas las compras ni consultar reportes globales -> 403
        mockMvc.perform(get("/api/compras")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/compras/reporte").param("periodo", "mes")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        // Administrador sí puede listar todas las compras (con o sin paginación) y consultar reportes -> 200
        mockMvc.perform(get("/api/compras")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        mockMvc.perform(get("/api/compras").param("page", "0").param("size", "10")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        mockMvc.perform(get("/api/compras/reporte").param("periodo", "mes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("mes"))
                .andExpect(jsonPath("$.totalOperaciones").value(4))
                .andExpect(jsonPath("$.montoTotalVendido").value(155000))
                .andExpect(jsonPath("$.serviciosMasSolicitados[0].nombre").value("Plan Nutricional Deportivo"));

        // Período inválido -> 400 ErrorResponse JSON
        mockMvc.perform(get("/api/compras/reporte").param("periodo", "trimestre")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"));

        // Paciente no puede cambiar estado de compra -> 403
        mockMvc.perform(put("/api/compras/5/estado").param("estado", "Completado")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        // Estado inválido -> 400 ErrorResponse JSON
        mockMvc.perform(put("/api/compras/5/estado").param("estado", "Pendiente")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"));

        // Compra inexistente -> 404
        mockMvc.perform(put("/api/compras/99/estado").param("estado", "Completado")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());

        // Administrador actualiza estado de compra -> 200
        mockMvc.perform(put("/api/compras/5/estado").param("estado", "Completado")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Completado"));
    }
}

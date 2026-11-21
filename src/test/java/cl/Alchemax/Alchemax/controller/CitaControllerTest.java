package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.GlobalExceptionHandler;
import cl.Alchemax.Alchemax.exception.HorarioNoDisponibleException;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaService citaService;

    private Cita citaEjemplo;
    private Usuario propietario;

    @BeforeEach
    void setUp() {
        propietario = new Usuario();
        propietario.setId(2L);
        propietario.setNombre("Camila Soto");
        propietario.setEmail("camila@gmail.com");

        citaEjemplo = new Cita(
                1L,
                LocalDate.of(2026, 11, 25),
                "10:30",
                "evaluacion",
                "Dra. Ana Pérez",
                "Programada"
        );
        citaEjemplo.setUsuario(propietario);
    }

    @Test
    void listarSinAutenticacionDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarYFiltrarGlobalmenteDebeRequerirRolAdminONutricionista() throws Exception {
        when(citaService.listar()).thenReturn(List.of(citaEjemplo));
        when(citaService.listarPorEstado("Programada")).thenReturn(List.of(citaEjemplo));
        when(citaService.listarPorProfesional("Dra. Ana Pérez")).thenReturn(List.of(citaEjemplo));
        when(citaService.listarPorFecha(LocalDate.of(2026, 11, 25))).thenReturn(List.of(citaEjemplo));

        // Paciente (ROLE_USUARIO) no puede listar todas las citas globales (PII)
        mockMvc.perform(get("/api/citas")
                        .with(jwt()
                                .jwt(j -> j.claim("rol", "USUARIO").claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/citas/estado/Programada")
                        .with(jwt()
                                .jwt(j -> j.claim("rol", "USUARIO").claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        // Nutricionista o Admin sí pueden listar y filtrar
        mockMvc.perform(get("/api/citas")
                        .with(jwt()
                                .jwt(j -> j.claim("rol", "NUTRICIONISTA").claim("usuarioId", 3L))
                                .authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/api/citas/estado/Programada")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/citas/profesional").param("profesional", "Dra. Ana Pérez")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/citas/fecha").param("fecha", "2026-11-25")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorUsuarioDebeRechazarTokenSinUsuarioIdYAccesoCruzado() throws Exception {
        when(citaService.listarPorUsuario(2L)).thenReturn(List.of(citaEjemplo));

        // Token sin claim usuarioId y sin rol privilegiado -> 403 (fail-closed)
        mockMvc.perform(get("/api/citas/usuario/2").with(jwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("403"));

        // Paciente usuarioId=99 intentando leer citas de usuarioId=2 -> 403
        mockMvc.perform(get("/api/citas/usuario/2")
                        .with(jwt().jwt(j -> j.claim("rol", "USUARIO").claim("usuarioId", 99L))))
                .andExpect(status().isForbidden());

        // Propietario usuarioId=2 -> 200
        mockMvc.perform(get("/api/citas/usuario/2")
                        .with(jwt().jwt(j -> j.claim("rol", "USUARIO").claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        // Nutricionista -> 200
        mockMvc.perform(get("/api/citas/usuario/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPorIdDebeRetornar404CuandoNoExisteYValidarPropiedad() throws Exception {
        when(citaService.buscarPorId(1L)).thenReturn(citaEjemplo);
        when(citaService.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/citas/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/citas/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Programada"));

        mockMvc.perform(get("/api/citas/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 55L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void guardarDebeVincularAlUsuarioDelTokenEImpedirSuplantacionPorPacienteEmail() throws Exception {
        when(citaService.guardar(any(Cita.class))).thenReturn(citaEjemplo);

        String bodyConEmailAjeno = """
                {
                  "fecha": "2026-11-25",
                  "hora": "10:30",
                  "tipo": "evaluacion",
                  "profesional": "Dra. Ana Pérez",
                  "estado": "Programada",
                  "pacienteEmail": "victima@gmail.com"
                }
                """;

        mockMvc.perform(post("/api/citas")
                        .with(jwt().jwt(j -> j.claim("rol", "USUARIO").claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyConEmailAjeno))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        ArgumentCaptor<Cita> captor = ArgumentCaptor.forClass(Cita.class);
        verify(citaService).guardar(captor.capture());
        Cita enviadaAlServicio = captor.getValue();
        assertNotNull(enviadaAlServicio.getUsuario());
        assertEquals(2L, enviadaAlServicio.getUsuario().getId());
        assertNull(enviadaAlServicio.getPacienteEmail());
    }

    @Test
    void guardarDebeRetornar400ConDatosInvalidosY409EnConflictoHorario() throws Exception {
        String bodyInvalido = """
                {
                  "fecha": "2026-11-25",
                  "hora": "hora-invalida",
                  "tipo": "evaluacion",
                  "profesional": "Dra. Ana Pérez",
                  "estado": "EstadoInvalido"
                }
                """;

        mockMvc.perform(post("/api/citas")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());

        when(citaService.guardar(any(Cita.class)))
                .thenThrow(new HorarioNoDisponibleException("Horario ocupado"));

        String bodyValido = """
                {
                  "fecha": "2026-11-25",
                  "hora": "10:30",
                  "tipo": "evaluacion",
                  "profesional": "Dra. Ana Pérez",
                  "estado": "Programada"
                }
                """;

        mockMvc.perform(post("/api/citas")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409"));
    }

    @Test
    void actualizarDebeVerificarExistenciaPrimeroYValidarPropiedad() throws Exception {
        when(citaService.buscarPorId(1L)).thenReturn(citaEjemplo);
        when(citaService.buscarPorId(99L)).thenReturn(null);
        when(citaService.actualizar(any(Cita.class), eq(1L))).thenReturn(citaEjemplo);

        String body = """
                {
                  "fecha": "2026-11-25",
                  "hora": "10:30",
                  "tipo": "evaluacion",
                  "profesional": "Dra. Ana Pérez",
                  "estado": "Programada"
                }
                """;

        // Si no existe -> 404 inmediato
        mockMvc.perform(put("/api/citas/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        // Otro paciente -> 403
        mockMvc.perform(put("/api/citas/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 77L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        // Propietario -> 200
        mockMvc.perform(put("/api/citas/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void cambiarEstadoDebeRestringirTransicionesSegunRolYPropiedad() throws Exception {
        Cita confirmada = new Cita(1L, LocalDate.of(2026, 11, 25), "10:30", "evaluacion", "Dra. Ana Pérez", "Confirmada");
        Cita cancelada = new Cita(1L, LocalDate.of(2026, 11, 25), "10:30", "evaluacion", "Dra. Ana Pérez", "Cancelada");

        when(citaService.buscarPorId(1L)).thenReturn(citaEjemplo);
        when(citaService.buscarPorId(99L)).thenReturn(null);
        when(citaService.cambiarEstado(1L, "Confirmada")).thenReturn(confirmada);
        when(citaService.cambiarEstado(1L, "Cancelada")).thenReturn(cancelada);

        // Paciente propietario puede cancelar su cita
        mockMvc.perform(put("/api/citas/1/estado").param("estado", "Cancelada")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Cancelada"));

        // Paciente NO puede marcar su propia cita como Confirmada
        mockMvc.perform(put("/api/citas/1/estado").param("estado", "Confirmada")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isForbidden());

        // Nutricionista sí puede confirmar
        mockMvc.perform(put("/api/citas/1/estado").param("estado", "Confirmada")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Confirmada"));

        mockMvc.perform(put("/api/citas/99/estado").param("estado", "Cancelada")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NUTRICIONISTA"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDebeValidarPropiedadYExistencia() throws Exception {
        when(citaService.buscarPorId(1L)).thenReturn(citaEjemplo);
        when(citaService.buscarPorId(99L)).thenReturn(null);
        when(citaService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/citas/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 88L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/citas/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/citas/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNotFound());
    }
}

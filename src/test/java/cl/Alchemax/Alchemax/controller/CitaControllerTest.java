package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
@Import(SecurityConfig.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitaService citaService;

    private Cita citaEjemplo;

    @BeforeEach
    void setUp() {
        citaEjemplo = new Cita(
                1L,
                LocalDate.of(2026, 11, 25),
                "10:30",
                "evaluacion",
                "Dra. Ana Pérez",
                "Programada"
        );
    }

    @Test
    void listarSinAutenticacionDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarConJwtDebeRetornar200() throws Exception {
        when(citaService.listar()).thenReturn(List.of(citaEjemplo));

        mockMvc.perform(get("/api/citas").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].profesional").value("Dra. Ana Pérez"));
    }

    @Test
    void obtenerPorIdDebeRetornar200CuandoExisteY404CuandoNo() throws Exception {
        when(citaService.buscarPorId(1L)).thenReturn(citaEjemplo);
        when(citaService.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/citas/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Programada"));

        mockMvc.perform(get("/api/citas/99").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorFiltrosDebeRetornar200() throws Exception {
        when(citaService.listarPorEstado("Programada")).thenReturn(List.of(citaEjemplo));
        when(citaService.listarPorProfesional("Dra. Ana Pérez")).thenReturn(List.of(citaEjemplo));
        when(citaService.listarPorFecha(LocalDate.of(2026, 11, 25))).thenReturn(List.of(citaEjemplo));

        mockMvc.perform(get("/api/citas/estado/Programada").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/api/citas/profesional").param("profesional", "Dra. Ana Pérez").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/api/citas/fecha").param("fecha", "2026-11-25").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void guardarDebeRetornar201ConDatosValidosY400ConEstadoInvalido() throws Exception {
        when(citaService.guardar(any(Cita.class))).thenReturn(citaEjemplo);

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
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        String bodyInvalido = """
                {
                  "fecha": "2026-11-25",
                  "hora": "10:30",
                  "tipo": "evaluacion",
                  "profesional": "Dra. Ana Pérez",
                  "estado": "EstadoInvalido"
                }
                """;

        mockMvc.perform(post("/api/citas")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarDebeRetornar200CuandoExisteY404CuandoNo() throws Exception {
        when(citaService.actualizar(any(Cita.class), eq(1L))).thenReturn(citaEjemplo);
        when(citaService.actualizar(any(Cita.class), eq(99L))).thenReturn(null);

        String body = """
                {
                  "fecha": "2026-11-25",
                  "hora": "10:30",
                  "tipo": "evaluacion",
                  "profesional": "Dra. Ana Pérez",
                  "estado": "Programada"
                }
                """;

        mockMvc.perform(put("/api/citas/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(put("/api/citas/99")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarEstadoDebeValidarEstadoYRetornarCodigoAdecuado() throws Exception {
        Cita completada = new Cita(1L, LocalDate.of(2026, 11, 25), "10:30", "evaluacion", "Dra. Ana Pérez", "Completada");
        when(citaService.cambiarEstado(1L, "Completada")).thenReturn(completada);
        when(citaService.cambiarEstado(99L, "Completada")).thenReturn(null);

        mockMvc.perform(put("/api/citas/1/estado").param("estado", "Completada").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Completada"));

        mockMvc.perform(put("/api/citas/1/estado").param("estado", "Desconocido").with(jwt()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/citas/99/estado").param("estado", "Completada").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDebeRetornar204CuandoExisteY404CuandoNo() throws Exception {
        when(citaService.eliminar(1L)).thenReturn(true);
        when(citaService.eliminar(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/citas/1").with(jwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/citas/99").with(jwt()))
                .andExpect(status().isNotFound());
    }
}

package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.ItemCarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemCarritoController.class)
@Import(SecurityConfig.class)
class ItemCarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemCarritoService itemCarritoService;

    private ItemCarrito itemEjemplo;

    @BeforeEach
    void setUp() {
        Servicio servicio = new Servicio();
        servicio.setId(20L);
        servicio.setNombre("Evaluación Nutricional");
        servicio.setPrecio(25000);

        itemEjemplo = new ItemCarrito(1L, servicio, 2, null);
    }

    @Test
    void listarSinAutenticacionDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/items-carrito"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarYListarPorCarritoDebenRetornar200() throws Exception {
        when(itemCarritoService.listar()).thenReturn(List.of(itemEjemplo));
        when(itemCarritoService.listarPorCarrito(10L)).thenReturn(List.of(itemEjemplo));

        mockMvc.perform(get("/api/items-carrito").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cantidad").value(2));

        mockMvc.perform(get("/api/items-carrito/carrito/10").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].servicio.nombre").value("Evaluación Nutricional"));
    }

    @Test
    void obtenerPorIdDebeRetornar200CuandoExisteY404CuandoNo() throws Exception {
        when(itemCarritoService.buscarPorId(1L)).thenReturn(itemEjemplo);
        when(itemCarritoService.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/items-carrito/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));

        mockMvc.perform(get("/api/items-carrito/99").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardarDebeRetornar201CuandoEsValidoY400CuandoNoExisteCarritoOServicio() throws Exception {
        when(itemCarritoService.guardar(10L, 20L, 2)).thenReturn(itemEjemplo);
        when(itemCarritoService.guardar(99L, 20L, 1)).thenReturn(null);

        mockMvc.perform(post("/api/items-carrito/carrito/10")
                        .param("servicioId", "20")
                        .param("cantidad", "2")
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(post("/api/items-carrito/carrito/99")
                        .param("servicioId", "20")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarDebeValidarCantidadYRetornarCodigoAdecuado() throws Exception {
        when(itemCarritoService.actualizar(any(ItemCarrito.class), eq(1L))).thenReturn(itemEjemplo);
        when(itemCarritoService.actualizar(any(ItemCarrito.class), eq(99L))).thenReturn(null);

        String bodyValido = """
                {
                  "cantidad": 3
                }
                """;

        mockMvc.perform(put("/api/items-carrito/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        String bodyInvalido = """
                {
                  "cantidad": 0
                }
                """;

        mockMvc.perform(put("/api/items-carrito/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/items-carrito/99")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDebeRetornar204CuandoExisteY404CuandoNo() throws Exception {
        when(itemCarritoService.eliminar(1L)).thenReturn(true);
        when(itemCarritoService.eliminar(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/items-carrito/1").with(jwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/items-carrito/99").with(jwt()))
                .andExpect(status().isNotFound());
    }
}

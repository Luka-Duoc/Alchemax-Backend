package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.GlobalExceptionHandler;
import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import cl.Alchemax.Alchemax.model.Servicio;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.ItemCarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ItemCarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemCarritoService itemCarritoService;

    private ItemCarrito itemEjemplo;
    private Carrito carritoEjemplo;

    @BeforeEach
    void setUp() {
        Usuario propietario = new Usuario();
        propietario.setId(2L);

        carritoEjemplo = new Carrito();
        carritoEjemplo.setId(10L);
        carritoEjemplo.setUsuario(propietario);

        Servicio servicio = new Servicio();
        servicio.setId(20L);
        servicio.setNombre("Evaluación Nutricional");
        servicio.setPrecio(25000);

        itemEjemplo = new ItemCarrito(1L, servicio, 2, carritoEjemplo);
    }

    @Test
    void listarSinAutenticacionDebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/items-carrito"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarYListarPorCarritoDebenValidarRolYPropiedad() throws Exception {
        when(itemCarritoService.listar()).thenReturn(List.of(itemEjemplo));
        when(itemCarritoService.obtenerCarrito(10L)).thenReturn(carritoEjemplo);
        when(itemCarritoService.listarPorCarrito(10L)).thenReturn(List.of(itemEjemplo));

        // Listar todos los ítems de todos los carritos requiere ROLE_ADMIN
        mockMvc.perform(get("/api/items-carrito")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))
                                .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/items-carrito")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cantidad").value(2));

        // Otro paciente no puede ver los ítems del carrito 10
        mockMvc.perform(get("/api/items-carrito/carrito/10")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 99L))))
                .andExpect(status().isForbidden());

        // Propietario sí puede ver los ítems del carrito 10
        mockMvc.perform(get("/api/items-carrito/carrito/10")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].servicio.nombre").value("Evaluación Nutricional"));
    }

    @Test
    void obtenerPorIdDebeValidarPropiedadYExistencia() throws Exception {
        when(itemCarritoService.buscarPorId(1L)).thenReturn(itemEjemplo);
        when(itemCarritoService.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/items-carrito/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));

        mockMvc.perform(get("/api/items-carrito/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 77L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/items-carrito/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardarDebeValidarPropiedadDelCarritoYRetornar201O400() throws Exception {
        when(itemCarritoService.obtenerCarrito(10L)).thenReturn(carritoEjemplo);
        when(itemCarritoService.obtenerCarrito(99L)).thenReturn(null);
        when(itemCarritoService.guardar(10L, 20L, 2)).thenReturn(itemEjemplo);

        // Otro usuario no puede agregar ítems al carrito 10
        mockMvc.perform(post("/api/items-carrito/carrito/10")
                        .param("servicioId", "20")
                        .param("cantidad", "2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 55L))))
                .andExpect(status().isForbidden());

        // Propietario -> 201
        mockMvc.perform(post("/api/items-carrito/carrito/10")
                        .param("servicioId", "20")
                        .param("cantidad", "2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        // Carrito inexistente -> 400 ErrorResponse JSON
        mockMvc.perform(post("/api/items-carrito/carrito/99")
                        .param("servicioId", "20")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"));
    }

    @Test
    void actualizarDebeValidarCantidadPropiedadYExistencia() throws Exception {
        when(itemCarritoService.buscarPorId(1L)).thenReturn(itemEjemplo);
        when(itemCarritoService.buscarPorId(99L)).thenReturn(null);
        when(itemCarritoService.actualizarExistente(eq(itemEjemplo), any(ItemCarrito.class))).thenReturn(itemEjemplo);

        String bodyValido = """
                {
                  "cantidad": 3
                }
                """;

        // Otro paciente -> 403
        mockMvc.perform(put("/api/items-carrito/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 88L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido))
                .andExpect(status().isForbidden());

        // Propietario -> 200
        mockMvc.perform(put("/api/items-carrito/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
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
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/items-carrito/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyValido))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDebeValidarPropiedadYExistencia() throws Exception {
        when(itemCarritoService.buscarPorId(1L)).thenReturn(itemEjemplo);
        when(itemCarritoService.buscarPorId(99L)).thenReturn(null);
        when(itemCarritoService.eliminarExistente(itemEjemplo)).thenReturn(true);

        mockMvc.perform(delete("/api/items-carrito/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 44L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/items-carrito/1")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/items-carrito/99")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isNotFound());
    }
}

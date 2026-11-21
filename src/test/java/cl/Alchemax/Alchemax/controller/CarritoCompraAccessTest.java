package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.exception.GlobalExceptionHandler;
import cl.Alchemax.Alchemax.model.Carrito;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.CarritoService;
import cl.Alchemax.Alchemax.service.CompraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        // Propietario (usuarioId=2) -> 200
        mockMvc.perform(get("/api/carrito/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void comprasDebeRechazarAccesoDeOtroUsuarioPermitirAlPropietarioYRestringirDeleteAAdmin() throws Exception {
        Compra compra = new Compra();
        compra.setId(5L);
        compra.setNombre("Plan Deportivo");
        when(compraService.listarPorUsuario(2L)).thenReturn(List.of(compra));

        mockMvc.perform(get("/api/compras/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 99L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("403"));

        mockMvc.perform(get("/api/compras/usuario/2")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        // Un paciente no puede eliminar registros de compras -> 403
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/compras/5")
                        .with(jwt().jwt(j -> j.claim("usuarioId", 2L))
                                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USUARIO"))))
                .andExpect(status().isForbidden());

        // Un administrador sí puede eliminar registros de compras -> 204
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/compras/5")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}

package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.model.Rol;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.security.JwtService;
import cl.Alchemax.Alchemax.security.SecurityConfig;
import cl.Alchemax.Alchemax.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuarioGuardado;

    @BeforeEach
    void setUp() {
        usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setNombre("Camila Soto");
        usuarioGuardado.setEmail("camila@gmail.com");
        usuarioGuardado.setGenero("femenino");
        usuarioGuardado.setFechaNacimiento("1994-05-12");
        usuarioGuardado.setRol(Rol.usuario);
        usuarioGuardado.setPassword(passwordEncoder.encode("Str0ng#Pass99"));
    }

    @Test
    void registerDebeCrearUsuarioConRolUsuarioYSinDobleEncriptacion() throws Exception {
        when(usuarioService.findbyEmail("nueva@gmail.com")).thenReturn(Optional.empty());
        when(usuarioService.guardar(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            // Verificar que AuthController no re-encriptó antes de delegar a UsuarioService
            assertEquals("Str0ng#Pass99", u.getPassword());
            assertEquals(Rol.usuario, u.getRol());
            u.setId(2L);
            u.setPassword(passwordEncoder.encode(u.getPassword()));
            return u;
        });

        String body = """
                {
                  "nombre": "Nueva Paciente",
                  "email": "NUEVA@gmail.com",
                  "genero": "prefiero-no-decirlo",
                  "fechaNacimiento": "1998-03-10",
                  "password": "Str0ng#Pass99",
                  "rol": "admin"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer token_invalido_de_swagger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("nueva@gmail.com"))
                .andExpect(jsonPath("$.rol").value("usuario"))
                .andExpect(jsonPath("$.genero").value("prefiero-no-decirlo"));

        verify(usuarioService).guardar(any(Usuario.class));
    }

    @Test
    void registerConCorreoDuplicadoDebeRetornar409() throws Exception {
        when(usuarioService.findbyEmail("camila@gmail.com")).thenReturn(Optional.of(usuarioGuardado));

        String body = """
                {
                  "nombre": "Camila Soto",
                  "email": "camila@gmail.com",
                  "genero": "femenino",
                  "fechaNacimiento": "1994-05-12",
                  "password": "Str0ng#Pass99"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginDebeRetornarJwtConCredencialesValidasY401ConInvalidas() throws Exception {
        when(usuarioService.findbyEmail("camila@gmail.com")).thenReturn(Optional.of(usuarioGuardado));
        when(usuarioService.verificarPassword(usuarioGuardado, "Str0ng#Pass99")).thenReturn(true);
        when(usuarioService.verificarPassword(usuarioGuardado, "WrongPassword1!")).thenReturn(false);

        String loginOk = """
                {
                  "email": "CAMILA@gmail.com",
                  "password": "Str0ng#Pass99"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", "Bearer token_expirado_o_malformado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginOk))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rol").value("usuario"))
                .andExpect(jsonPath("$.genero").value("femenino"));

        String loginBad = """
                {
                  "email": "camila@gmail.com",
                  "password": "WrongPassword1!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBad))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerConDatosInvalidosDebeRetornar400YNo401() throws Exception {
        String bodyInvalido = """
                {
                  "nombre": "",
                  "email": "correo-invalido",
                  "genero": "genero-no-permitido",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", "Bearer basura_en_swagger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400"));
    }

    @Test
    void meDebeRequerirJwtYRetornarPerfilSinPassword() throws Exception {
        when(usuarioService.findbyEmail("camila@gmail.com")).thenReturn(Optional.of(usuarioGuardado));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject("camila@gmail.com").claim("rol", "USUARIO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("camila@gmail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateMeDebeActualizarPerfilYValidarPasswordActual() throws Exception {
        when(usuarioService.findbyEmail("camila@gmail.com")).thenReturn(Optional.of(usuarioGuardado));
        when(usuarioService.verificarPassword(usuarioGuardado, "Incorrecta123!")).thenReturn(false);
        when(usuarioService.editar(any(Usuario.class), eq(1L))).thenAnswer(inv -> {
            Usuario cambios = inv.getArgument(0);
            usuarioGuardado.setNombre(cambios.getNombre());
            usuarioGuardado.setPesoActual(cambios.getPesoActual());
            return usuarioGuardado;
        });

        String updateBody = """
                {
                  "nombre": "Camila Actualizada",
                  "pesoActual": 66
                }
                """;

        mockMvc.perform(put("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject("camila@gmail.com").claim("rol", "USUARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Camila Actualizada"))
                .andExpect(jsonPath("$.pesoActual").value(66));

        String badPassChange = """
                {
                  "passwordActual": "Incorrecta123!",
                  "nuevaPassword": "NewStr0ng#Pass99"
                }
                """;

        mockMvc.perform(put("/api/auth/me")
                        .with(jwt().jwt(j -> j.subject("camila@gmail.com").claim("rol", "USUARIO")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPassChange))
                .andExpect(status().isBadRequest());
    }
}

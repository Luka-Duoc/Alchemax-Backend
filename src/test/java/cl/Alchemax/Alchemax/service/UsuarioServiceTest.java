package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Rol;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Camila Soto");
        usuario.setEmail("camila@gmail.com");
        usuario.setGenero("femenino");
        usuario.setRol(Rol.usuario);
    }

    @Test
    void guardarDebeEncriptarUnaSolaVezYPermitirVerificarPassword() {
        usuario.setPassword("Str0ng#Pass99");
        when(usuarioRepository.findByEmailIgnoreCase("camila@gmail.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.guardar(usuario);

        assertNotNull(guardado);
        assertTrue(UsuarioService.isBcryptHash(guardado.getPassword()));
        assertTrue(usuarioService.verificarPassword(guardado, "Str0ng#Pass99"));
        assertFalse(usuarioService.verificarPassword(guardado, "OtraClave99!"));
    }

    @Test
    void verificarPasswordDebeAceptarYActualizarPasswordLegacyEnTextoPlano() {
        usuario.setPassword("admin123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(usuarioService.verificarPassword(usuario, "admin123"));
        assertTrue(UsuarioService.isBcryptHash(usuario.getPassword()));
        verify(usuarioRepository).save(usuario);
        assertTrue(usuarioService.verificarPassword(usuario, "admin123"));
    }
}

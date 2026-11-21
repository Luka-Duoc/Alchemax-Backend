package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static boolean isBcryptHash(String value) {
        return value != null
                && value.length() >= 59
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    // Listar
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    // Guardar
    public Usuario guardar(Usuario u) {
        u.setId(null);
        String emailNormalizado = u.getEmail() != null ? u.getEmail().trim().toLowerCase() : "";
        u.setEmail(emailNormalizado);

        if (usuarioRepository.findByEmailIgnoreCase(emailNormalizado).isPresent()) {
            return null;
        }
        if (!isBcryptHash(u.getPassword())) {
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        }
        return usuarioRepository.save(u);
    }

    // Editar
    public Usuario editar(Usuario u, Long id) {
        return usuarioRepository.findById(id).map(existente -> {
            existente.setNombre(u.getNombre());
            if (u.getEmail() != null) {
                existente.setEmail(u.getEmail().trim().toLowerCase());
            }
            if (u.getRol() != null) {
                existente.setRol(u.getRol());
            }
            existente.setGenero(u.getGenero());
            existente.setFechaNacimiento(u.getFechaNacimiento());
            existente.setPesoActual(u.getPesoActual());
            existente.setObjetivoPrincipal(u.getObjetivoPrincipal());
            existente.setFotoPerfil(u.getFotoPerfil());

            if (u.getPassword() != null && !u.getPassword().isBlank()) {
                if (!isBcryptHash(u.getPassword())) {
                    existente.setPassword(passwordEncoder.encode(u.getPassword()));
                } else {
                    existente.setPassword(u.getPassword());
                }
            }

            return usuarioRepository.save(existente);
        }).orElse(null);
    }

    // Eliminar
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean verificarPassword(Usuario usuario, String rawPassword) {
        if (usuario == null || rawPassword == null || usuario.getPassword() == null) {
            return false;
        }

        String stored = usuario.getPassword();
        if (isBcryptHash(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        // Compatibilidad con registros antiguos en BD guardados en texto plano:
        // si coincide, migra automáticamente a BCrypt al iniciar sesión.
        boolean matchesLegacy = MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8)
        );
        if (matchesLegacy) {
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuario);
        }
        return matchesLegacy;
    }

    public Usuario login(String email, String password) {
        if (email == null || password == null) return null;
        return findbyEmail(email)
                .filter(u -> verificarPassword(u, password))
                .orElse(null);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Optional<Usuario> findbyEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return usuarioRepository.findByEmailIgnoreCase(email.trim());
    }
}

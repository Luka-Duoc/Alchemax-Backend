package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Listar
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    // Guardar
    public Usuario guardar(Usuario u) {
        String emailNormalizado = u.getEmail() != null ? u.getEmail().trim().toLowerCase() : "";
        u.setEmail(emailNormalizado);

        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            return null;
        }
        u.setPassword(passwordEncoder.encode(u.getPassword()));
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
                existente.setPassword(passwordEncoder.encode(u.getPassword()));
            }

            return usuarioRepository.save(existente);
        }).orElse(null);
    }

    // Eliminar
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario login(String email, String password) {
        if (email == null || password == null) return null;
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElse(null);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Optional<Usuario> findbyEmail(String email) {
        if (email == null) return Optional.empty();
        return usuarioRepository.findByEmail(email.trim().toLowerCase());
    }
}

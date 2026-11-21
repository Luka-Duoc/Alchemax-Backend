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

    //Listar
    public List<Usuario> listar(){
        return usuarioRepository.findAll();
    }

    //Guardar
    public Usuario guardar(Usuario u) {
        if (usuarioRepository.findByEmail(u.getEmail()).isPresent()) {
            return null;
        }
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return usuarioRepository.save(u);
    }

    //Editar
    public Usuario editar(Usuario u, Long id) {
        return usuarioRepository.findById(id).map(existente -> {
            existente.setNombre(u.getNombre());
            existente.setEmail(u.getEmail());
            existente.setRol(u.getRol());
            existente.setGenero(u.getGenero());
            existente.setFechaNacimiento(u.getFechaNacimiento());
            existente.setPesoActual(u.getPesoActual());
            existente.setObjetivoPrincipal(u.getObjetivoPrincipal());

            if (u.getPassword() != null && !u.getPassword().isBlank()) {
                existente.setPassword(passwordEncoder.encode(u.getPassword()));
            }

            return usuarioRepository.save(existente);
        }).orElse(null);
    }

    //Eliminar
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario login(String email, String password) {
        return usuarioRepository.findByEmailAndPassword(email, password).orElse(null);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Optional<Usuario> findbyEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

}

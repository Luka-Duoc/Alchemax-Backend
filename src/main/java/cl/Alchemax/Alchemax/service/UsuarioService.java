package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //Listar
    public List<Usuario> listar(){
        return usuarioRepository.findAll();
    }

    //Guardar
    public Usuario guardar(Usuario u) {
        return usuarioRepository.save(u);
    }

    //Editar
    public Usuario editar(Usuario u, Long id) {
        Usuario actualizar = usuarioRepository.findById(id).orElse(null);
        if(actualizar == null) return null;
        actualizar.setNombre(u.getNombre());
        actualizar.setEmail(u.getEmail());
        actualizar.setRol(u.getRol());
        actualizar.setPesoActual(u.getPesoActual());
        actualizar.setObjetivoPrincipal(u.getObjetivoPrincipal());

        return usuarioRepository.save(actualizar);
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

}

package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.repository.CompraRepository;
import cl.Alchemax.Alchemax.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Listar
    public List<Compra> listarPorUsuario(Long usuarioId) {
        return compraRepository.findByUsuario_Id(usuarioId);
    }

    //Guardar
    public Compra guardar(Long usuarioId, Compra compra) {
        Usuario user = usuarioRepository.findById(usuarioId).orElse(null);
        if (user == null) {
            return null;
        }

        compra.setUsuario(user);

        if (compra.getFechaCompra() == null) {
            compra.setFechaCompra(LocalDate.now());
        }
        if (compra.getEstado() == null || compra.getEstado().isBlank()) {
            compra.setEstado("Activo");
        }

        return compraRepository.save(compra);
    }

    //Eliminar
    public boolean eliminar(Long id) {
        if (compraRepository.existsById(id)) {
            compraRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

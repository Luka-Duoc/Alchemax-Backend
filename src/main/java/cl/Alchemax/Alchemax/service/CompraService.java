package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    // Listar
    public List<Compra> listarPorUsuario(Long usuarioId) {
        return compraRepository.findByUsuario_Id(usuarioId);
    }

    //Guardar
    public Compra guardar(Compra c) {
        return compraRepository.save(c);
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

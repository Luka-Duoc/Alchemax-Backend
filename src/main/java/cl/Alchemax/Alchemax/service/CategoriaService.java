package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.model.Categoria;
import cl.Alchemax.Alchemax.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    //Listar
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // Guardar
    public Categoria guardar(Categoria c) {
        return categoriaRepository.save(c);
    }

    // Actualizar
    public Categoria actualizar(Categoria c, Long id) {
        Categoria actualizar = categoriaRepository.findById(id).orElse(null);
        if ( actualizar == null) return null;

        actualizar.setNombre(c.getNombre());
        actualizar.setDescripcion(c.getDescripcion());

        return categoriaRepository.save(actualizar);
    }

    // Eliminar
    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}

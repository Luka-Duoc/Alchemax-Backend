package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuario_Id(Long usuarioId);
}

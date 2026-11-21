package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
    List<ItemCarrito> findByCarrito_Id(Long carritoId);

    Optional<ItemCarrito> findByCarrito_IdAndServicio_Id(Long carritoId, Long servicioId);
}

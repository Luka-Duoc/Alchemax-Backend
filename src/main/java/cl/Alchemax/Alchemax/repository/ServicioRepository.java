package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByNombre(String nombre);
}

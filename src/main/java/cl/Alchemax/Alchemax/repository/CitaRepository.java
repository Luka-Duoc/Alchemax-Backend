package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByEstado(String estado);

    List<Cita> findByProfesional(String profesional);

    List<Cita> findByFecha(LocalDate fecha);
}

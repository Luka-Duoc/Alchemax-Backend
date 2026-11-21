package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByEstado(String estado);

    List<Cita> findByProfesional(String profesional);

    List<Cita> findByFecha(LocalDate fecha);

    List<Cita> findByUsuario_Id(Long usuarioId);

    @Query("""
        select count(c) > 0 from Cita c
        where c.fecha = :fecha
          and c.hora = :hora
          and lower(trim(c.profesional)) = lower(trim(:profesional))
          and c.estado <> 'Cancelada'
          and (:excluirId is null or c.id <> :excluirId)
    """)
    boolean existeConflictoHorario(
            @Param("profesional") String profesional,
            @Param("fecha") LocalDate fecha,
            @Param("hora") String hora,
            @Param("excluirId") Long excluirId
    );
}

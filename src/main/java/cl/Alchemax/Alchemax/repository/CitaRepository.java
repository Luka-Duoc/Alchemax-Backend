package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
          and c.profesional = :profesional
          and c.hora = :hora
          and c.slotActivo = 1
          and (:excluirId is null or c.id <> :excluirId)
    """)
    boolean existeConflictoHorario(
            @Param("profesional") String profesional,
            @Param("fecha") LocalDate fecha,
            @Param("hora") String hora,
            @Param("excluirId") Long excluirId
    );

    @Modifying
    @Transactional
    @Query("update Cita c set c.estado = 'Programada' where c.estado is null or c.estado = ''")
    int normalizarEstadosNulos();

    @Modifying
    @Transactional
    @Query("update Cita c set c.slotActivo = null where c.estado = 'Cancelada' and c.slotActivo is not null")
    int limpiarSlotActivoCanceladas();

    @Query("select c from Cita c where (c.estado <> 'Cancelada' and c.slotActivo is null) or length(c.hora) <> 5")
    List<Cita> findCitasPendientesDeNormalizar();
}

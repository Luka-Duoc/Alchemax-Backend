package cl.Alchemax.Alchemax.repository;

import cl.Alchemax.Alchemax.model.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuario_Id(Long usuarioId);

    List<Compra> findByUsuario_IdOrderByFechaCompraDescIdDesc(Long usuarioId);

    List<Compra> findAllByOrderByFechaCompraDescIdDesc();

    Page<Compra> findAllByOrderByFechaCompraDescIdDesc(Pageable pageable);

    @Query("""
        select c.estado, count(c), coalesce(sum(c.monto), 0)
        from Compra c
        where (:inicio is null or c.fechaCompra >= :inicio)
          and (:fin is null or c.fechaCompra <= :fin)
        group by c.estado
    """)
    List<Object[]> contarYSumarPorEstadoEnRango(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );

    @Query("""
        select c.servicio, max(c.nombre), count(c), coalesce(sum(c.monto), 0)
        from Compra c
        where (c.estado is null or c.estado <> 'Cancelado')
          and (:inicio is null or c.fechaCompra >= :inicio)
          and (:fin is null or c.fechaCompra <= :fin)
        group by c.servicio
        order by count(c) desc, coalesce(sum(c.monto), 0) desc
    """)
    List<Object[]> obtenerRankingServiciosPorRango(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}

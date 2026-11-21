package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.dto.ReporteAdminResponse;
import cl.Alchemax.Alchemax.dto.ServicioSolicitadoDTO;
import cl.Alchemax.Alchemax.exception.SolicitudInvalidaException;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import cl.Alchemax.Alchemax.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ReporteAdminService {

    public static final Set<String> PERIODOS_VALIDOS = Set.of(
            "semana",
            "mes",
            "anio",
            "historico"
    );

    @Autowired
    private CompraRepository compraRepository;

    @Autowired(required = false)
    private CitaRepository citaRepository;

    public ReporteAdminResponse generarReporte(String periodoRaw) {
        if (periodoRaw == null || periodoRaw.isBlank()) {
            throw new SolicitudInvalidaException("El período debe ser semana, mes, anio o historico.");
        }

        String periodo = periodoRaw.trim().toLowerCase(Locale.ROOT);
        if (!PERIODOS_VALIDOS.contains(periodo)) {
            throw new SolicitudInvalidaException("El período debe ser semana, mes, anio o historico.");
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = null;
        LocalDate fechaFin = null;

        switch (periodo) {
            case "semana":
                fechaInicio = hoy.minusDays(6);
                fechaFin = hoy;
                break;
            case "mes":
                fechaInicio = hoy.minusDays(29);
                fechaFin = hoy;
                break;
            case "anio":
                fechaInicio = LocalDate.of(hoy.getYear(), 1, 1);
                fechaFin = hoy;
                break;
            case "historico":
            default:
                break;
        }

        long totalCompras = 0L;
        long comprasActivas = 0L;
        long comprasCompletadas = 0L;
        long comprasCanceladas = 0L;
        long montoTotalVendido = 0L;

        List<Object[]> resumenCompras = compraRepository.contarYSumarPorEstadoEnRango(fechaInicio, fechaFin);
        if (resumenCompras != null) {
            for (Object[] fila : resumenCompras) {
                String estado = fila[0] != null ? fila[0].toString() : Compra.ESTADO_ACTIVO;
                long cantidad = fila[1] instanceof Number n ? n.longValue() : 0L;
                long sumaMonto = fila[2] instanceof Number n ? Math.max(0L, n.longValue()) : 0L;

                totalCompras += cantidad;
                if (Compra.ESTADO_CANCELADO.equalsIgnoreCase(estado)) {
                    comprasCanceladas += cantidad;
                } else {
                    montoTotalVendido += sumaMonto;
                    if (Compra.ESTADO_COMPLETADO.equalsIgnoreCase(estado)) {
                        comprasCompletadas += cantidad;
                    } else {
                        comprasActivas += cantidad;
                    }
                }
            }
        }

        long comprasValidasVenta = totalCompras - comprasCanceladas;
        long ticketPromedio = comprasValidasVenta > 0
                ? Math.round((double) montoTotalVendido / comprasValidasVenta)
                : 0L;

        long totalCitas = 0L;
        long citasProgramadas = 0L;
        long citasConfirmadas = 0L;
        long citasCompletadas = 0L;
        long citasCanceladas = 0L;

        if (citaRepository != null) {
            List<Object[]> resumenCitas = citaRepository.contarPorEstadoEnRango(fechaInicio, fechaFin);
            if (resumenCitas != null) {
                for (Object[] fila : resumenCitas) {
                    String estado = fila[0] != null ? fila[0].toString() : Cita.ESTADO_PROGRAMADA;
                    long cantidad = fila[1] instanceof Number n ? n.longValue() : 0L;

                    totalCitas += cantidad;
                    if (Cita.ESTADO_CANCELADA.equalsIgnoreCase(estado)) {
                        citasCanceladas += cantidad;
                    } else if (Cita.ESTADO_COMPLETADA.equalsIgnoreCase(estado)) {
                        citasCompletadas += cantidad;
                    } else if (Cita.ESTADO_CONFIRMADA.equalsIgnoreCase(estado)) {
                        citasConfirmadas += cantidad;
                    } else {
                        citasProgramadas += cantidad;
                    }
                }
            }
        }

        long totalOperaciones = totalCompras + totalCitas;
        long serviciosRealizados = comprasCompletadas + citasCompletadas;

        List<ServicioSolicitadoDTO> ranking = new ArrayList<>();
        List<Object[]> filasRanking = compraRepository.obtenerRankingServiciosPorRango(fechaInicio, fechaFin);
        if (filasRanking != null) {
            for (Object[] fila : filasRanking) {
                int servicioId = fila[0] instanceof Number n ? n.intValue() : 0;
                String nombre = (fila[1] != null && !fila[1].toString().isBlank())
                        ? fila[1].toString().trim()
                        : "Servicio #" + servicioId;
                long cantidadVendida = fila[2] instanceof Number n ? n.longValue() : 0L;
                long montoRecaudado = fila[3] instanceof Number n ? Math.max(0L, n.longValue()) : 0L;
                double pct = comprasValidasVenta > 0
                        ? Math.round((cantidadVendida * 1000.0) / comprasValidasVenta) / 10.0
                        : 0.0;

                ranking.add(new ServicioSolicitadoDTO(
                        servicioId,
                        nombre,
                        cantidadVendida,
                        montoRecaudado,
                        pct
                ));
            }
        }

        return new ReporteAdminResponse(
                periodo,
                fechaInicio,
                fechaFin,
                totalOperaciones,
                totalCompras,
                comprasActivas,
                comprasCompletadas,
                comprasCanceladas,
                montoTotalVendido,
                ticketPromedio,
                totalCitas,
                citasProgramadas,
                citasConfirmadas,
                citasCompletadas,
                citasCanceladas,
                serviciosRealizados,
                ranking
        );
    }
}

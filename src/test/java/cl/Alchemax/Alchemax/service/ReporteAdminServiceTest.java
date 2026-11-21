package cl.Alchemax.Alchemax.service;

import cl.Alchemax.Alchemax.dto.ReporteAdminResponse;
import cl.Alchemax.Alchemax.exception.SolicitudInvalidaException;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.Compra;
import cl.Alchemax.Alchemax.repository.CitaRepository;
import cl.Alchemax.Alchemax.repository.CompraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteAdminServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ReporteAdminService reporteAdminService;

    @Test
    void generarReporteDebeCalcularIndicadoresDesdeAgregacionesSQLYValidarVentanasTemporales() {
        LocalDate hoy = LocalDate.now();

        List<Object[]> resumenCompras = List.of(
                new Object[]{Compra.ESTADO_ACTIVO, 1L, 120000L},
                new Object[]{Compra.ESTADO_COMPLETADO, 2L, 155000L},
                new Object[]{Compra.ESTADO_CANCELADO, 1L, 25000L}
        );
        List<Object[]> resumenCitas = List.of(
                new Object[]{Cita.ESTADO_PROGRAMADA, 1L},
                new Object[]{Cita.ESTADO_COMPLETADA, 1L}
        );
        List<Object[]> rankingSql = List.of(
                new Object[]{2, "Plan Nutricional Deportivo", 2L, 240000L},
                new Object[]{1, "Evaluación Biométrica Inicial", 1L, 35000L}
        );

        when(compraRepository.contarYSumarPorEstadoEnRango(any(), any())).thenReturn(resumenCompras);
        when(citaRepository.contarPorEstadoEnRango(any(), any())).thenReturn(resumenCitas);
        when(compraRepository.obtenerRankingServiciosPorRango(any(), any())).thenReturn(rankingSql);

        // 1. Período "semana": [hoy - 6d, hoy] (7 días históricos exactos)
        ReporteAdminResponse repSemana = reporteAdminService.generarReporte("semana");
        assertEquals("semana", repSemana.getPeriodo());
        assertEquals(hoy.minusDays(6), repSemana.getFechaInicio());
        assertEquals(hoy, repSemana.getFechaFin());
        assertEquals(6L, repSemana.getTotalOperaciones()); // 4 compras + 2 citas
        assertEquals(4L, repSemana.getTotalCompras());
        assertEquals(1L, repSemana.getComprasActivas());
        assertEquals(2L, repSemana.getComprasCompletadas());
        assertEquals(1L, repSemana.getComprasCanceladas());
        assertEquals(275000L, repSemana.getMontoTotalVendido()); // 120000 + 155000 (excluye Cancelado)
        assertEquals(Math.round(275000.0 / 3.0), repSemana.getTicketPromedio());
        assertEquals(3L, repSemana.getServiciosRealizados()); // 2 compras completadas + 1 cita completada
        assertEquals(2, repSemana.getServiciosMasSolicitados().size());
        assertEquals(2, repSemana.getServiciosMasSolicitados().get(0).getServicioId());
        assertEquals(66.7, repSemana.getServiciosMasSolicitados().get(0).getPorcentajeVentas());

        // 2. Período "mes": [hoy - 29d, hoy] (30 días históricos exactos)
        ReporteAdminResponse repMes = reporteAdminService.generarReporte("mes");
        assertEquals(hoy.minusDays(29), repMes.getFechaInicio());
        assertEquals(hoy, repMes.getFechaFin());

        // 3. Período "anio": [1 de enero, hoy]
        ReporteAdminResponse repAnio = reporteAdminService.generarReporte("anio");
        assertEquals(LocalDate.of(hoy.getYear(), 1, 1), repAnio.getFechaInicio());
        assertEquals(hoy, repAnio.getFechaFin());

        // 4. Período "historico": sin límites de fecha
        ReporteAdminResponse repHist = reporteAdminService.generarReporte("historico");
        assertNull(repHist.getFechaInicio());
        assertNull(repHist.getFechaFin());
    }

    @Test
    void generarReporteConBaseDeDatosVaciaYCitaRepositoryNullNoDebeDividirPorCero() {
        when(compraRepository.contarYSumarPorEstadoEnRango(any(), any())).thenReturn(List.of());
        when(compraRepository.obtenerRankingServiciosPorRango(any(), any())).thenReturn(List.of());
        ReflectionTestUtils.setField(reporteAdminService, "citaRepository", null);

        ReporteAdminResponse vacio = reporteAdminService.generarReporte("historico");

        assertEquals(0L, vacio.getTotalOperaciones());
        assertEquals(0L, vacio.getMontoTotalVendido());
        assertEquals(0L, vacio.getTicketPromedio());
        assertTrue(vacio.getServiciosMasSolicitados().isEmpty());
    }

    @Test
    void generarReporteConPeriodoInvalidoDebeLanzarSolicitudInvalidaException() {
        assertThrows(SolicitudInvalidaException.class, () -> reporteAdminService.generarReporte("trimestre"));
        assertThrows(SolicitudInvalidaException.class, () -> reporteAdminService.generarReporte(""));
        assertThrows(SolicitudInvalidaException.class, () -> reporteAdminService.generarReporte(null));
    }
}

package cl.Alchemax.Alchemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteAdminResponse {
    private String periodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long totalOperaciones;
    private long totalCompras;
    private long comprasActivas;
    private long comprasCompletadas;
    private long comprasCanceladas;
    private long montoTotalVendido;
    private long ticketPromedio;
    private long totalCitas;
    private long citasProgramadas;
    private long citasConfirmadas;
    private long citasCompletadas;
    private long citasCanceladas;
    private long serviciosRealizados;
    private List<ServicioSolicitadoDTO> serviciosMasSolicitados;
}

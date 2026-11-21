package cl.Alchemax.Alchemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicioSolicitadoDTO {
    private int servicioId;
    private String nombre;
    private long cantidadVendida;
    private long montoRecaudado;
    private double porcentajeVentas;
}

package cl.Alchemax.Alchemax.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "compra",
        indexes = {
                @Index(name = "idx_compra_fecha_compra", columnList = "fecha_compra")
        }
)
public class Compra {

    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_COMPLETADO = "Completado";
    public static final String ESTADO_CANCELADO = "Cancelado";
    public static final String ESTADOS_REGEX = "Activo|Completado|Cancelado";

    public static final Set<String> ESTADOS_VALIDOS = Set.of(
            ESTADO_ACTIVO,
            ESTADO_COMPLETADO,
            ESTADO_CANCELADO
    );

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Positive(message = "El identificador del servicio debe ser positivo")
    private int servicio;

    @NotBlank(message = "El nombre del servicio comprado es obligatorio")
    private String nombre;

    private LocalDate fechaCompra;

    @Min(value = 0, message = "El monto de la compra no puede ser negativo")
    private int monto;

    @Pattern(
            regexp = ESTADOS_REGEX,
            message = "El estado debe ser Activo, Completado o Cancelado"
    )
    private String estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"password", "carrito", "compras", "fotoPerfil"})
    private Usuario usuario;
}

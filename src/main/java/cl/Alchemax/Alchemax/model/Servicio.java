package cl.Alchemax.Alchemax.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.action.internal.OrphanRemovalAction;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Servicio {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotBlank(message = "El nombre del servicio no puede estar en blanco")
    private String nombre;
    @NotBlank(message = "La descripcion no puede estar vacia")
    private String descripcion;
    @NotNull(message = "El precio no puede ser nulo")
    @Positive(message = "El precio no puede ser menor a 0")
    private int precio;
    @Pattern(regexp = "evaluacion|plan|control")
    private String tipo;
    private String icono;
    @ManyToOne
    @JoinColumn(name = "categoriaId")
    private Categoria categoriaId;
    private String categoriaNombre;
    private Boolean estado = true;
}

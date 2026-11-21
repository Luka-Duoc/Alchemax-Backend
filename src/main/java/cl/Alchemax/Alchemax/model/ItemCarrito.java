package cl.Alchemax.Alchemax.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "item_carrito",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_carrito_servicio",
                        columnNames = {"carrito_id", "servicio_id"}
                )
        }
)
public class ItemCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private int cantidad = 1;
    @ManyToOne()
    @JoinColumn(name = "carrito_id")
    @JsonIgnore
    private Carrito carrito;
}

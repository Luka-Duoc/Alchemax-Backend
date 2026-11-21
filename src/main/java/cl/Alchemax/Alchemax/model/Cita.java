package cl.Alchemax.Alchemax.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Cita {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    private LocalDate fecha;
    private String hora;
    private String tipo;
    private String profesional;

    @Pattern(regexp = "Programada|Confirmada|Completada|Cancelada")
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"password", "carrito", "compras", "fotoPerfil"})
    private Usuario usuario;

    private String pacienteNombre;
    private String pacienteEmail;

    public Cita(Long id, LocalDate fecha, String hora, String tipo, String profesional, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.profesional = profesional;
        this.estado = estado;
    }
}

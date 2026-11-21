package cl.Alchemax.Alchemax.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "cita",
        indexes = {
                @Index(name = "idx_cita_fecha_prof_hora", columnList = "fecha, profesional, hora")
        }
)
public class Cita {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "La fecha de la cita es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "La hora de la cita es obligatoria")
    @Pattern(
            regexp = "^([01]?\\d|2[0-3]):[0-5]\\d(:00)?$",
            message = "Formato de hora inválido (use HH:mm)"
    )
    private String hora;

    @NotBlank(message = "El tipo de cita es obligatorio")
    private String tipo;

    @NotBlank(message = "El profesional es obligatorio")
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
        this.hora = normalizarHora(hora);
        this.tipo = tipo;
        this.profesional = profesional;
        this.estado = estado;
    }

    public static String normalizarHora(String rawHora) {
        if (rawHora == null) {
            return null;
        }
        String trimmed = rawHora.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        String[] parts = trimmed.split(":");
        if (parts.length >= 2) {
            String hh = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
            String mm = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
            return hh + ":" + mm;
        }
        return trimmed;
    }
}

package cl.Alchemax.Alchemax.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "cita",
        indexes = {
                @Index(name = "idx_cita_fecha_prof_hora", columnList = "fecha, profesional, hora")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cita_slot_activo",
                        columnNames = {"fecha", "profesional", "hora", "slot_activo"}
                )
        }
)
public class Cita {

    public static final String ESTADO_PROGRAMADA = "Programada";
    public static final String ESTADO_CONFIRMADA = "Confirmada";
    public static final String ESTADO_COMPLETADA = "Completada";
    public static final String ESTADO_CANCELADA = "Cancelada";
    public static final String ESTADOS_REGEX = "Programada|Confirmada|Completada|Cancelada";
    public static final Set<String> ESTADOS_VALIDOS = Set.of(
            ESTADO_PROGRAMADA,
            ESTADO_CONFIRMADA,
            ESTADO_COMPLETADA,
            ESTADO_CANCELADA
    );

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

    @Pattern(regexp = ESTADOS_REGEX)
    private String estado;

    /**
     * Validador de unicidad en base de datos para citas activas:
     * vale 1 cuando la cita está activa y NULL cuando está Cancelada
     * (en SQL, múltiples NULL no colisionan en un índice UNIQUE).
     */
    @JsonIgnore
    @Column(name = "slot_activo")
    private Integer slotActivo = 1;

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
        setEstado(estado);
    }

    public void setEstado(String estado) {
        this.estado = estado;
        sincronizarSlotActivo();
    }

    @PrePersist
    @PreUpdate
    public void sincronizarSlotActivo() {
        this.hora = normalizarHora(this.hora);
        if (this.profesional != null) {
            this.profesional = this.profesional.trim();
        }
        if (this.estado == null || this.estado.isBlank()) {
            this.estado = ESTADO_PROGRAMADA;
        }
        this.slotActivo = ESTADO_CANCELADA.equalsIgnoreCase(this.estado) ? null : 1;
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

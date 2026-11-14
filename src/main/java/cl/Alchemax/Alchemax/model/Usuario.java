package cl.Alchemax.Alchemax.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Usuario {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @NotBlank(message = "El nombre no puede estar vacio")
    private String nombre;
    @NotBlank(message = "El email no puede estar vacio")
    @Column(unique = true, name = "email")
    private String email;
    @Pattern(regexp = "(?i)hombre|masculino|mujer|femenino|otro")
    private String genero;
    @NotBlank(message = "La contraseña no puede estar vacio")
    private String password;
    private String fechaNacimiento;
    @Pattern(regexp = "admin|usuario|nutricionista")
    private String rol;
    private Integer pesoActual;
    private String objetivoPrincipal;

}

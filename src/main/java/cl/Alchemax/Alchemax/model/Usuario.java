package cl.Alchemax.Alchemax.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Pattern(regexp = "(?i)hombre|masculino|mujer|femenino|otro|prefiero-no-decirlo")
    private String genero;

    @NotBlank(message = "La contraseña no puede estar vacio")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.usuario;

    private Integer pesoActual;
    private String objetivoPrincipal;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fotoPerfil;

    // Indica que usuario es dueño del carrito por lo que si se borra al usuario se elimina el carrito relacionado
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Carrito carrito;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Compra> compras = new ArrayList<>();
}

package cl.Alchemax.Alchemax.dto;

import cl.Alchemax.Alchemax.model.Rol;
import cl.Alchemax.Alchemax.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private String genero;
    private String fechaNacimiento;
    private Integer pesoActual;
    private String objetivoPrincipal;
    private String fotoPerfil;

    public AuthResponse(String token, Long id, String nombre, String email, Rol rol) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public static AuthResponse from(String token, Usuario u) {
        return new AuthResponse(
                token,
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol(),
                u.getGenero(),
                u.getFechaNacimiento(),
                u.getPesoActual(),
                u.getObjetivoPrincipal(),
                u.getFotoPerfil()
        );
    }
}

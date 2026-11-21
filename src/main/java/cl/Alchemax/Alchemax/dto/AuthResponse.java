package cl.Alchemax.Alchemax.dto;

import cl.Alchemax.Alchemax.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;

}

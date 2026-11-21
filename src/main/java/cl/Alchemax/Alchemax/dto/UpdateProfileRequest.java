package cl.Alchemax.Alchemax.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nombre;
    private String email;
    private String genero;
    private String fechaNacimiento;
    private Integer pesoActual;
    private String objetivoPrincipal;
    private String fotoPerfil;
    private String passwordActual;
    private String nuevaPassword;
}

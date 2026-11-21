package cl.Alchemax.Alchemax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterRequest {

    @Schema(description = "Nombre completo del paciente", example = "Juan Pérez")
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@gmail.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Schema(description = "Contraseña de acceso", example = "Password123!")
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    @Schema(
            description = "Género del usuario (masculino, femenino, otro, prefiero-no-decirlo)",
            example = "masculino"
    )
    private String genero;

    @Schema(description = "Fecha de nacimiento (YYYY-MM-DD)", example = "1995-05-12")
    private String fechaNacimiento;
}

package cl.Alchemax.Alchemax.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @Schema(description = "Correo electrónico registrado", example = "admin@gmail.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Schema(description = "Contraseña del usuario", example = "admin123")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}

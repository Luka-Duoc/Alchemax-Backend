package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.dto.AuthResponse;
import cl.Alchemax.Alchemax.dto.LoginRequest;
import cl.Alchemax.Alchemax.exception.ErrorResponse;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.security.JwtService;
import cl.Alchemax.Alchemax.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Autenticación", description = "Endpoints públicos para registro e inicio de sesión con JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con contraseña encriptada y devuelve su token JWT.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Usuario usuario) {
        if (usuarioService.findbyEmail(usuario.getEmail()).isPresent()) {
            ErrorResponse error = new ErrorResponse(
                    "400",
                    "Correo duplicado",
                    "El correo electrónico ya se encuentra registrado.",
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(error);
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario guardado = usuarioService.guardar(usuario);
        String token = jwtService.generarToken(guardado);

        AuthResponse response = new AuthResponse(
                token,
                guardado.getId(),
                guardado.getNombre(),
                guardado.getEmail(),
                guardado.getRol()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve el token JWT junto con los datos básicos del usuario.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.findbyEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .<ResponseEntity<?>>map(u -> {
                    String token = jwtService.generarToken(u);
                    AuthResponse response = new AuthResponse(
                            token,
                            u.getId(),
                            u.getNombre(),
                            u.getEmail(),
                            u.getRol()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    ErrorResponse error = new ErrorResponse(
                            "401",
                            "No autorizado",
                            "Correo electrónico o contraseña incorrectos.",
                            LocalDateTime.now()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                });
    }

}

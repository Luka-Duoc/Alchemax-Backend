package cl.Alchemax.Alchemax.controller;

import cl.Alchemax.Alchemax.dto.AuthResponse;
import cl.Alchemax.Alchemax.dto.LoginRequest;
import cl.Alchemax.Alchemax.dto.UpdateProfileRequest;
import cl.Alchemax.Alchemax.exception.ErrorResponse;
import cl.Alchemax.Alchemax.model.Rol;
import cl.Alchemax.Alchemax.model.Usuario;
import cl.Alchemax.Alchemax.security.JwtService;
import cl.Alchemax.Alchemax.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Tag(name = "Autenticación", description = "Endpoints para registro, inicio de sesión y perfil con JWT")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con rol paciente y contraseña encriptada, devolviendo su token JWT.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Usuario usuario) {
        String emailNormalizado = usuario.getEmail().trim().toLowerCase();
        usuario.setEmail(emailNormalizado);
        usuario.setNombre(usuario.getNombre().trim());
        // Seguridad: todo registro público se crea estrictamente con rol usuario
        usuario.setRol(Rol.usuario);

        if (usuarioService.findbyEmail(emailNormalizado).isPresent()) {
            ErrorResponse error = new ErrorResponse(
                    "409",
                    "Correo duplicado",
                    "El correo electrónico ya se encuentra registrado.",
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // UsuarioService.guardar encripta la contraseña con BCrypt una sola vez
        Usuario guardado = usuarioService.guardar(usuario);
        if (guardado == null) {
            ErrorResponse error = new ErrorResponse(
                    "409",
                    "Correo duplicado",
                    "El correo electrónico ya se encuentra registrado.",
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        String token = jwtService.generarToken(guardado);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(token, guardado));
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve el token JWT junto con los datos del usuario.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        return usuarioService.findbyEmail(emailNormalizado)
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .<ResponseEntity<?>>map(u -> {
                    String token = jwtService.generarToken(u);
                    return ResponseEntity.ok(AuthResponse.from(token, u));
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

    @Operation(summary = "Obtener perfil actual", description = "Devuelve los datos del usuario autenticado a partir de su token JWT.")
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return usuarioService.findbyEmail(authentication.getName().trim().toLowerCase())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Actualizar perfil propio", description = "Permite al usuario autenticado actualizar sus propios datos y opcionalmente cambiar su contraseña.")
    @PutMapping("/me")
    public ResponseEntity<?> updateMe(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentEmail = authentication.getName().trim().toLowerCase();
        Optional<Usuario> existenteOpt = usuarioService.findbyEmail(currentEmail);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Usuario existente = existenteOpt.get();

        String nuevoEmail = request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail().trim().toLowerCase()
                : existente.getEmail();

        if (!nuevoEmail.equalsIgnoreCase(existente.getEmail())) {
            Optional<Usuario> otro = usuarioService.findbyEmail(nuevoEmail);
            if (otro.isPresent() && !otro.get().getId().equals(existente.getId())) {
                ErrorResponse error = new ErrorResponse(
                        "409",
                        "Correo duplicado",
                        "El correo electrónico ya está en uso por otra cuenta.",
                        LocalDateTime.now()
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }

        if (request.getNuevaPassword() != null && !request.getNuevaPassword().isBlank()) {
            if (request.getPasswordActual() == null
                    || !passwordEncoder.matches(request.getPasswordActual(), existente.getPassword())) {
                ErrorResponse error = new ErrorResponse(
                        "400",
                        "Contraseña actual incorrecta",
                        "La contraseña actual ingresada es incorrecta.",
                        LocalDateTime.now()
                );
                return ResponseEntity.badRequest().body(error);
            }
        }

        Usuario cambios = new Usuario();
        cambios.setNombre(request.getNombre() != null && !request.getNombre().isBlank()
                ? request.getNombre().trim()
                : existente.getNombre());
        cambios.setEmail(nuevoEmail);
        cambios.setGenero(request.getGenero() != null ? request.getGenero() : existente.getGenero());
        cambios.setFechaNacimiento(request.getFechaNacimiento() != null
                ? request.getFechaNacimiento()
                : existente.getFechaNacimiento());
        cambios.setPesoActual(request.getPesoActual() != null
                ? request.getPesoActual()
                : existente.getPesoActual());
        cambios.setObjetivoPrincipal(request.getObjetivoPrincipal() != null
                ? request.getObjetivoPrincipal()
                : existente.getObjetivoPrincipal());
        cambios.setFotoPerfil(request.getFotoPerfil());
        // Nunca permitir cambiar el propio rol desde /api/auth/me
        cambios.setRol(existente.getRol());
        if (request.getNuevaPassword() != null && !request.getNuevaPassword().isBlank()) {
            cambios.setPassword(request.getNuevaPassword());
        }

        Usuario actualizado = usuarioService.editar(cambios, existente.getId());
        if (actualizado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String nuevoToken = jwtService.generarToken(actualizado);
        return ResponseEntity.ok(AuthResponse.from(nuevoToken, actualizado));
    }
}

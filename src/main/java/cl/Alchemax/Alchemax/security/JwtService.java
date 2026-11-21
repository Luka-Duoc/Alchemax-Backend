package cl.Alchemax.Alchemax.security;

import cl.Alchemax.Alchemax.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;
    private final String issuer;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expiration.minutes}") long expirationMinutes,
            @Value("${app.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora =  Instant.now();

        cl.Alchemax.Alchemax.model.Rol rolEfectivo =
                usuario.getRol() != null ? usuario.getRol() : cl.Alchemax.Alchemax.model.Rol.usuario;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(ahora)
                .expiresAt(
                        ahora.plus(Duration.ofMinutes(expirationMinutes))
                )
                .subject(usuario.getEmail())
                .claim("nombre", usuario.getNombre())
                .claim("rol", rolEfectivo.name().toUpperCase())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}

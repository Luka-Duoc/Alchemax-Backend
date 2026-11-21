package cl.Alchemax.Alchemax.security;

import cl.Alchemax.Alchemax.exception.SesionExpiradaException;
import cl.Alchemax.Alchemax.model.Rol;
import cl.Alchemax.Alchemax.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class JwtService {

    private static final Duration MAX_SESSION_AGE = Duration.ofDays(7);

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
        Instant ahora = Instant.now();
        return emitirToken(usuario, ahora, ahora.getEpochSecond());
    }

    public String renovarToken(Usuario usuario, Jwt tokenActual) {
        Instant ahora = Instant.now();
        long origIatEpoch = ahora.getEpochSecond();

        if (tokenActual != null) {
            Object origClaim = tokenActual.getClaim("orig_iat");
            if (origClaim != null) {
                try {
                    origIatEpoch = Long.parseLong(origClaim.toString());
                } catch (NumberFormatException ignored) {
                    origIatEpoch = ahora.getEpochSecond();
                }
            } else if (tokenActual.getIssuedAt() != null) {
                origIatEpoch = tokenActual.getIssuedAt().getEpochSecond();
            }
        }

        Instant inicioSesion = Instant.ofEpochSecond(origIatEpoch);
        if (Duration.between(inicioSesion, ahora).compareTo(MAX_SESSION_AGE) > 0) {
            throw new SesionExpiradaException(
                    "El tiempo máximo de la sesión ha expirado. Inicia sesión nuevamente."
            );
        }

        return emitirToken(usuario, ahora, origIatEpoch);
    }

    private String emitirToken(Usuario usuario, Instant ahora, long origIatEpoch) {
        if (usuario.getId() == null) {
            throw new IllegalArgumentException(
                    "El usuario debe tener un ID persistido para emitir un token JWT."
            );
        }

        Rol rolEfectivo = usuario.getRol() != null ? usuario.getRol() : Rol.usuario;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofMinutes(expirationMinutes)))
                .subject(usuario.getEmail())
                .claim("nombre", usuario.getNombre())
                .claim("rol", rolEfectivo.name().toUpperCase(Locale.ROOT))
                .claim("usuarioId", usuario.getId())
                .claim("orig_iat", origIatEpoch)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}

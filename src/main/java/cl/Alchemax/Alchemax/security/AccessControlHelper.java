package cl.Alchemax.Alchemax.security;

import cl.Alchemax.Alchemax.exception.AccesoDenegadoException;
import cl.Alchemax.Alchemax.model.Cita;
import cl.Alchemax.Alchemax.model.ItemCarrito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public final class AccessControlHelper {

    private AccessControlHelper() {
    }

    public static boolean esAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public static boolean esAdminONutricionista(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ROLE_NUTRICIONISTA".equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<Long> obtenerUsuarioIdDelToken(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object claimId = jwt.getClaim("usuarioId");
            if (claimId != null) {
                try {
                    return Optional.of(Long.valueOf(claimId.toString()));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Verifica de forma estricta (fail-closed) que el usuario autenticado sea propietario
     * del usuarioId solicitado o posea rol ADMIN / NUTRICIONISTA.
     */
    public static boolean puedeAccederRecursoUsuario(Authentication authentication, Long targetUsuarioId) {
        if (authentication == null || targetUsuarioId == null) {
            return false;
        }
        if (esAdminONutricionista(authentication)) {
            return true;
        }
        return obtenerUsuarioIdDelToken(authentication)
                .map(id -> id.equals(targetUsuarioId))
                .orElse(false);
    }

    public static void verificarAccesoUsuario(Authentication authentication, Long targetUsuarioId, String mensajeError) {
        if (!puedeAccederRecursoUsuario(authentication, targetUsuarioId)) {
            throw new AccesoDenegadoException(mensajeError);
        }
    }

    /**
     * Verifica de forma estricta que el usuario autenticado sea el titular del recurso comercial
     * (carrito, ítems de carrito o compras) o posea rol ADMIN (excluyendo a NUTRICIONISTA).
     */
    public static boolean puedeAccederRecursoComercialUsuario(Authentication authentication, Long targetUsuarioId) {
        if (authentication == null || targetUsuarioId == null) {
            return false;
        }
        if (esAdmin(authentication)) {
            return true;
        }
        return obtenerUsuarioIdDelToken(authentication)
                .map(id -> id.equals(targetUsuarioId))
                .orElse(false);
    }

    public static void verificarAccesoComercialUsuario(Authentication authentication, Long targetUsuarioId, String mensajeError) {
        if (!puedeAccederRecursoComercialUsuario(authentication, targetUsuarioId)) {
            throw new AccesoDenegadoException(mensajeError);
        }
    }

    public static void verificarAccesoCita(Authentication authentication, Cita cita, String mensajeError) {
        if (authentication == null || cita == null) {
            throw new AccesoDenegadoException(mensajeError);
        }
        if (esAdminONutricionista(authentication)) {
            return;
        }
        Long propietarioId = cita.getUsuario() != null ? cita.getUsuario().getId() : null;
        if (propietarioId == null || !puedeAccederRecursoUsuario(authentication, propietarioId)) {
            throw new AccesoDenegadoException(mensajeError);
        }
    }

    public static void verificarAccesoItemCarrito(Authentication authentication, ItemCarrito item, String mensajeError) {
        if (authentication == null || item == null) {
            throw new AccesoDenegadoException(mensajeError);
        }
        if (esAdmin(authentication)) {
            return;
        }
        Long propietarioId = (item.getCarrito() != null && item.getCarrito().getUsuario() != null)
                ? item.getCarrito().getUsuario().getId()
                : null;
        if (propietarioId == null || !puedeAccederRecursoComercialUsuario(authentication, propietarioId)) {
            throw new AccesoDenegadoException(mensajeError);
        }
    }
}

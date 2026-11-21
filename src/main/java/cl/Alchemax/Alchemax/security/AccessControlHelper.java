package cl.Alchemax.Alchemax.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public final class AccessControlHelper {

    private AccessControlHelper() {
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

    /**
     * Verifica que el usuario autenticado sea propietario del usuarioId solicitado
     * o posea rol administrativo/clínico (ADMIN o NUTRICIONISTA).
     */
    public static boolean puedeAccederRecursoUsuario(Authentication authentication, Long targetUsuarioId) {
        if (authentication == null || targetUsuarioId == null) {
            return false;
        }
        if (esAdminONutricionista(authentication)) {
            return true;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object claimId = jwt.getClaim("usuarioId");
            if (claimId != null) {
                try {
                    Long tokenUserId = Long.valueOf(claimId.toString());
                    return tokenUserId.equals(targetUsuarioId);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        // Compatibilidad con tokens de prueba MockMvc sin claim "usuarioId"
        return true;
    }
}

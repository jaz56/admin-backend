package com.yassmine.administration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yassmine.administration.model.PermissionDetail;
import com.yassmine.administration.model.User;
import com.yassmine.administration.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j // Génère automatiquement un logger log.info/log.error
public class AdminPermissionFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    // 🟢 Table de correspondance : nom du segment d'URL -> clé de permission attendue
    // Un champ static final DOIT être déclaré au niveau de la classe, jamais dans une méthode.
    private static final Map<String, String> MODULE_ALIASES = Map.of(
            "bookings", "entretiens",
            "admin", "admins"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        log.info("🔍 [FILTER] Requête entrante - Méthode: {} | URI: {}", method, uri);

        // 1. Ignorer les routes publiques
        if (uri.startsWith("/api/auth") || uri.startsWith("/actuator") || uri.startsWith("/uploads") || method.equals("OPTIONS")) {
            log.info("🔓 [FILTER] Route publique détectée. Passage autorisé.");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("⚠️ [FILTER] Requête non authentifiée dans le contexte Spring Security.");
            filterChain.doFilter(request, response);
            return;
        }

        String email = auth.getName();
        log.info("👤 [FILTER] Utilisateur authentifié détecté par JWT: {}", email);

        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) {
            log.error("❌ [FILTER] Utilisateur {} introuvable en Base de données !", email);
            sendForbiddenError(response, "Utilisateur introuvable.");
            return;
        }

        // 🟢 DEBUG LOG DU TYPE ET DE LA VALEUR DE ISSUPERADMIN
        log.info("⚙️ [FILTER] ID: {} | Rôle en BD: {} | isSuperAdmin: {}",
                currentUser.getId(), currentUser.getRole(), currentUser.isSuperAdmin());

        // 3. Le Super Admin outrepasse tout
        if (currentUser.isSuperAdmin()) {
            log.info("👑 [FILTER] Accès accordé d'office : Utilisateur est Super Admin !");
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Blocage actif
        if (currentUser.isBlocked()) {
            log.warn("🚫 [FILTER] L'utilisateur {} est marqué comme BLOQUÉ.", email);
            if (currentUser.getBlockedUntil() != null) {
                if (LocalDate.now().isAfter(currentUser.getBlockedUntil())) {
                    currentUser.setBlocked(false);
                    currentUser.setBlockedUntil(null);
                    userRepository.save(currentUser);
                    log.info("🔓 [FILTER] Période de suspension écoulée. Compte réactivé automatiquement.");
                } else {
                    log.warn("❌ [FILTER] Rejet : Suspension temporaire active.");
                    sendForbiddenError(response, "Accès suspendu jusqu'au : " + currentUser.getBlockedUntil());
                    return;
                }
            } else {
                log.warn("❌ [FILTER] Rejet : Suspension permanente active.");
                sendForbiddenError(response, "Votre compte a été banni indéfiniment.");
                return;
            }
        }

        // 5. Expiration globale
        if (currentUser.getPermissionsExpiryDate() != null && LocalDate.now().isAfter(currentUser.getPermissionsExpiryDate())) {
            log.warn("❌ [FILTER] Rejet : Date d'expiration des permissions dépassée.");
            sendForbiddenError(response, "Vos droits d'accès ont expiré.");
            return;
        }

        // 6. Validation par module
        String[] uriSegments = uri.split("/");
        if (uriSegments.length > 2 && "api".equals(uriSegments[1])) {
            String module = uriSegments[2];
            // 🟢 Applique l'alias si le segment d'URL diffère de la clé de permission
            module = MODULE_ALIASES.getOrDefault(module, module);

            Map<String, PermissionDetail> permissions = currentUser.getPermissions();
            log.info("📦 [FILTER] Analyse du module '{}' pour l'utilisateur '{}'", module, email);

            if (permissions == null || !permissions.containsKey(module)) {
                log.warn("❌ [FILTER] Rejet : Aucune permission configurée pour le module '{}'", module);
                sendForbiddenError(response, "Accès refusé au module : " + module);
                return;
            }

            PermissionDetail detail = permissions.get(module);
            log.info("🔑 [FILTER] Droits détectés sur '{}' -> Read: {}, Create: {}, Update: {}",
                    module, detail.isRead(), detail.isCreate(), detail.isUpdate());

            if (method.equalsIgnoreCase("GET") && !detail.isRead()) {
                log.warn("❌ [FILTER] Rejet : Lecture interdite sur le module '{}'", module);
                sendForbiddenError(response, "Action interdite : Lecture non autorisée.");
                return;
            }
            if (method.equalsIgnoreCase("POST") && !detail.isCreate()) {
                log.warn("❌ [FILTER] Rejet : Création interdite sur le module '{}'", module);
                sendForbiddenError(response, "Action interdite : Création non autorisée.");
                return;
            }
            if ((method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH") || method.equalsIgnoreCase("DELETE")) && !detail.isUpdate()) {
                log.warn("❌ [FILTER] Rejet : Modification/Suppression interdite sur le module '{}'", module);
                sendForbiddenError(response, "Action interdite : Modification/Suppression non autorisée.");
                return;
            }
        }

        log.info("✅ [FILTER] Toutes les vérifications sont validées. Requête acceptée.");
        filterChain.doFilter(request, response);
    }

    private void sendForbiddenError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorDetails = Map.of(
                "timestamp", new Date().getTime(),
                "status", HttpServletResponse.SC_FORBIDDEN,
                "error", "Forbidden",
                "message", message
        );
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetails));
    }
}
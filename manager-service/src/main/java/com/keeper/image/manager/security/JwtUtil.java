package com.keeper.image.manager.security;

import com.nimbusds.jose.shaded.json.JSONArray;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Util class for reading and handling authentication information
 */
@Component
@NoArgsConstructor
public class JwtUtil {

    public static final String REALM_ACCESS = "realm_access";
    public static final String ROLES_KEY = "roles";
    public static final String PREFERRED_USERNAME = "preferred_username";

    /**
     * Read username from authentication information
     * @param authentication information about current authentication
     * @return user name
     */
    public String readUserName(Authentication authentication) {
        Jwt principal = getJwtPrincipal((JwtAuthenticationToken) authentication);

        return principal.getClaimAsString(PREFERRED_USERNAME);
    }

    /**
     * Read user roles from authentication information
     * @param authentication information about current authentication
     * @return List of user roles
     */
    public List<UserRole> readUserRoles(Authentication authentication) {
        Jwt principal = getJwtPrincipal((JwtAuthenticationToken) authentication);
        JSONArray roleArray = (JSONArray) principal.getClaimAsMap(REALM_ACCESS).get(ROLES_KEY);

        List<UserRole> userRoles = Arrays.stream(UserRole.values()).sequential()
                .filter(userRole -> roleArray.contains(userRole.getRoleName()))
                .collect(Collectors.toList());

        return userRoles;
    }

    private Jwt getJwtPrincipal(JwtAuthenticationToken authentication) {
        JwtAuthenticationToken jwtAuthenticationToken = authentication;
        return (Jwt) jwtAuthenticationToken.getPrincipal();
    }


}

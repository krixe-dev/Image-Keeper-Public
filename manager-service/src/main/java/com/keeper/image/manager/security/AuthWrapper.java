package com.keeper.image.manager.security;

import lombok.*;

import java.util.List;

/**
 * Wrapper class for storing user name and user roles
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AuthWrapper {

    private String userName;
    private List<UserRole> userRolesList;

    /**
     * Check if user has role
     * @param userRole user role
     * @return
     */
    public boolean checkRole(UserRole userRole) {
        return userRolesList.contains(userRole);
    }


}

package com.keeper.image.manager.security;

/**
 * User role in system
 */
public enum UserRole {

    ADMIN("system_admin"),
    USER("system_user");

    private String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}

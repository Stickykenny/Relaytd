package me.stky.relaytd.config;


import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Roles {
    ROLE_VISITOR,
    ROLE_USER,
    ROLE_OAUTH_USER,
    ROLE_ADMIN;

    public static SimpleGrantedAuthority toAuthority(Roles role) {
        return new SimpleGrantedAuthority(role.name());
    }

    public String getAuthorityName() {
        String prefix = "ROLE_";
        return this.toString().replace(prefix, "");
    }

}

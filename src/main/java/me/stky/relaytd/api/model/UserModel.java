package me.stky.relaytd.api.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class UserModel implements UserDetails {
    private String username;
    private String password;
    private List<GrantedAuthority> authorities;

    public UserModel(UserInfo user){
        this.username = user.getUsername();
        this.password = user.getPassword();
        List<GrantedAuthority> authorities = Stream.of(user.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority( role))
                .collect(Collectors.toList());
        this.authorities = Stream.of(user.getRoles().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return this.authorities; }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
package me.stky.relaytd.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * This controller is purely for testing
 */
@RestController
@RequestMapping("/test")
public class HelloController {

    @Operation(summary = "Get all users", description = "Returns a list of users")
    @GetMapping("/open")
    String testNoRoleEndpoint() {
        return "open";
    }

    @Operation(summary = "Get all users", description = "Returns a list of users")
    @GetMapping("/users")
    String getUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Collection<SimpleGrantedAuthority> list = (Collection<SimpleGrantedAuthority>) auth.getAuthorities();

        for (SimpleGrantedAuthority permission : list) {
            System.out.println(permission.getAuthority());
        }
        System.out.println("Logged-in User: " + auth.getName());
        System.out.println("Authorities: " + auth.getAuthorities());
        return List.of("Alice", "Bob", "Charlie").toString() + list.toString();
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Get all users", description = "Returns a list of users")
    @GetMapping("/users2")
    String getUsers_admin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Collection<SimpleGrantedAuthority> list = (Collection<SimpleGrantedAuthority>) auth.getAuthorities();

        for (SimpleGrantedAuthority permission : list) {
            System.out.println(permission.getAuthority());
        }
        System.out.println("Logged-in User: " + auth.getName());
        System.out.println("Authorities: " + auth.getAuthorities());
        return List.of("Alice", "Bob", "Charlie").toString() + " ADMIN " + list.toString();

    }

    //throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Too many rows : , use paginated.");

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String getUser() {
        return "User page";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String getAdmin() {
        return "Admin page";
    }

    @GetMapping("/visitor")
    @PreAuthorize("hasAuthority('ROLE_VISITOR')")
    public String getVisitor() {
        return "Visitor page";
    }
}
package me.stky.relaytd.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    public JwtCookieAuthenticationFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {

                System.out.println(" cookie: " + cookie);
                if ("jwt".equals(cookie.getName()) && jwtService.validateToken(cookie.getValue())) {
                    Jwt jwt = jwtService.jwtDecoder().decode(cookie.getValue());
                    String username = jwtService.extractUsername(cookie.getValue());

                    System.out.println("Extracted username: " + username);
                    cookie.getAttributes().entrySet().stream()
                            .forEach(entry -> System.out.println(entry.getKey() + " = " + entry.getValue()));

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    jwt.getClaimAsStringList("roles").stream().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("auth ok" + auth);
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

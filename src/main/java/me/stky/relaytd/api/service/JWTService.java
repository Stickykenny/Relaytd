package me.stky.relaytd.api.service;


import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.config.Roles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JWTService {


    @Value("${spring.security.jwt.key}")
    private String jwtKey;

    @Value("${spring.security.jwt.name}")
    private String jwtName;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private AuthentificationService authentificationService;

    public JWTService(AuthentificationService authentificationService, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.authentificationService = authentificationService;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Generate roles string for Jwt Token
     *
     * @param authentication
     * @return
     */
    public List<String> createRoles(Authentication authentication) {
        List<String> authorities = new ArrayList<>();
        if (authentication instanceof OAuth2AuthenticationToken) {
            List<GrantedAuthority> newAuthorities = List.of(
                    new SimpleGrantedAuthority(Roles.ROLE_OAUTH_USER.name()),
                    Roles.toAuthority(Roles.ROLE_USER), // Showing both method to create authority
                    new SimpleGrantedAuthority("ROLE_customedroleforgiggles")
            );
            authorities = newAuthorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("Authentification token isn't supported");
        }
        log.debug("Roles attributed : " + authorities);

        return authorities;
    }

    /**
     * Generate Jwt Token
     *
     * @param authentication
     * @return
     */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(60, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("roles", createRoles(authentication))
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        log.info("Generated this jwt token :" + this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue());
        claims.getClaims().forEach((key, value) -> System.out.println(key + ":" + value));
        return this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    public ResponseCookie generateCookie(String jwtToken) {
        return ResponseCookie.from(jwtName, jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(60))
                .sameSite("Lax") // or "Strict" or "None" or "Lax"
                .build();
    }

    public ResponseCookie invalidateCookie() {
        return ResponseCookie.from(jwtName, "invalidated")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(1))
                .sameSite("Lax") // or "Strict" or "None" or "Lax"
                .build();
    }


    public String extractUsername(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            this.jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
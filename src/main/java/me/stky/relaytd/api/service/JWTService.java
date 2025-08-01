package me.stky.relaytd.api.service;


import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.config.Roles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JWTService {

    private JwtEncoder jwtEncoder;

    private AuthentificationService authentificationService;

    public JWTService(JwtEncoder jwtEncoder, AuthentificationService authentificationService) {
        this.jwtEncoder = jwtEncoder;
        this.authentificationService = authentificationService;
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

}
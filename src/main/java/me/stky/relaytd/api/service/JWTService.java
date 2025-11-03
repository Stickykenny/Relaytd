package me.stky.relaytd.api.service;


import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.UserInfo;
import me.stky.relaytd.api.repository.UserRepository;
import me.stky.relaytd.config.Roles;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Random;
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

    @Autowired
    private UserRepository userRepository;

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
     * Generate Jwt Token with a validation key
     *
     * @param authentication
     * @return
     */
    public String generateToken(Authentication authentication) {

        String validationKey = "";
        if (userRepository.findByUsername(authentificationService.fetchUsernameFromAuth(authentication)).isPresent()) {
            validationKey = updateNewValidationKey(authentication);
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(60, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim("roles", createRoles(authentication))
                .claim("validationkey", validationKey)
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
                .sameSite("None") // or "Strict" or "None" or "Lax"
                .build();
    }

    public ResponseCookie invalidateCookie() {
        return ResponseCookie.from(jwtName, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None") // or "Strict" or "None" or "Lax"
                .maxAge(0)
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


    /**
     * Generate a random string of length 'length' including characters, numbers and special characters
     *
     * @param length Length of the string
     * @return A random string
     */
    public String generateRandomValidationKey(int length) {
        StringBuilder validationKey = new StringBuilder(length);
        Random random = new Random();
        int lowerLimit = 33;
        int upperLimit = 126; // included

        for (int i = 0; i < length; i++) {
            int randomIndex = lowerLimit + (int) (random.nextFloat() * (upperLimit - lowerLimit + 1));
            validationKey.append((char) randomIndex);
        }
        return validationKey.toString();
    }


    /**
     * Will provide and save a new validation key (for refresh token)
     *
     * @param authentication that will use the validation key
     * @return a new validation key
     */
    public String updateNewValidationKey(Authentication authentication) {
        String randomValidationKey = generateRandomValidationKey(128);

        String username = authentificationService.fetchUsernameFromAuth(authentication);
        UserInfo user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Could not find user [" + username + "] for changing its validation key."));

        user.setValidation_key(randomValidationKey);
        log.debug("Generated this validation key [" + randomValidationKey + "], for this user [" + user.getUsername() + "]");
        userRepository.save(user);
        return randomValidationKey;
    }
}
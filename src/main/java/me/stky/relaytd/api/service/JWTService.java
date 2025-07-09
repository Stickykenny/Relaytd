package me.stky.relaytd.api.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class JWTService {

    private JwtEncoder jwtEncoder;

    private AuthentificationService authentificationService;

    public JWTService(JwtEncoder jwtEncoder, AuthentificationService authentificationService) {
        this.jwtEncoder = jwtEncoder;
        this.authentificationService = authentificationService;
    }

    public String generateToken(Authentication authentication) {

        Instant now = Instant.now();
        this.authentificationService.getUserInfo(authentication, null);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(60, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                //.claim("roles")
                .build();
        JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        log.info("Generated this jwt token :" + this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue());
        return this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

}
package me.stky.relaytd.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;


public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;

    public JwtLoginSuccessHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String jwt = jwtService.generateToken(authentication);




        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax") // or "Strict" or "None"
                .build();
        System.out.println( cookie.toString());
        //response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        //response.addCookie(jwtCookie);

        // Redirect to Angular frontend
        response.sendRedirect("http://localhost:4200/oauth-callback/?token="+jwt); // Bad practice to put it so visible
        //response.sendRedirect("http://localhost:8080/homepage");
    }
}

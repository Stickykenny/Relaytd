package me.stky.relaytd.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Duration;


@Slf4j
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;

    public JwtLoginSuccessHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("Connecting using OAuth");
        String jwt = jwtService.generateToken(authentication);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .sameSite("Lax") // or "Strict" or "None" or "Lax"
                .build();
        log.debug(cookie.toString());
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Redirect to Angular frontend
        //response.sendRedirect("http://localhost:4200/oauth-callback/?token=" + jwt); // Bad practice to put it so visible
        response.sendRedirect("http://localhost:4200/oauth-callback/");
    }
}

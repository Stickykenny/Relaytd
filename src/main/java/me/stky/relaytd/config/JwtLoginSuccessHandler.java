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
        ResponseCookie cookie = jwtService.generateCookie(jwt);
        log.debug(cookie.toString());
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Redirect to Angular frontend
        response.sendRedirect("http://localhost:4200/oauth-callback/");
    }
}

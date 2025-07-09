package me.stky.relaytd.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.LoginRequest;
import me.stky.relaytd.api.service.AuthentificationService;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;

@Controller
@CrossOrigin
@Slf4j
public final class LoginController {


    @Autowired
    private AuthenticationManager authenticationManager;


    private AuthentificationService authentificationService;

    private JWTService jwtService;

    public LoginController(JWTService jwtService, AuthenticationManager authenticationManager, AuthentificationService authentificationService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.authentificationService = authentificationService;
    }

    //public String getToken(Authentication authentication) {

    @PostMapping("/login2")
    @ResponseBody // Required else thymeleaf search for it's template
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        log.info("Connecting using Form login");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword())
            );

            String token = jwtService.generateToken(authentication);
            return ResponseEntity.ok(token);

        } catch (AuthenticationException e) {
            log.info("login failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

/*
// Old endpoint with Thymeleaf
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            HttpServletRequest request,
                            Model model) {


        String userAgent = request.getHeader("User-Agent");
        String xfHeader = request.getHeader("X-Forwarded-For");
        String clientIp = request.getRemoteAddr();

        // Add attributes for Thymeleaf
        model.addAttribute("clientIp", clientIp);
        model.addAttribute("userAgent", userAgent);

        if (error != null) model.addAttribute("errorMsg", "Invalid username or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You've been logged out.");

        return "login";
    }


 */


    // Old endpoint with Thymeleaf
    @GetMapping("/homepage")
    String getHomepage(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Collection<SimpleGrantedAuthority> list = (Collection<SimpleGrantedAuthority>) auth.getAuthorities();

        // Get User-Agent and Client IP
        String userAgent = request.getHeader("User-Agent");
        String xfHeader = request.getHeader("X-Forwarded-For");
        String clientIp = request.getRemoteAddr();

        // Add attributes for Thymeleaf
        model.addAttribute("clientIp", clientIp);
        model.addAttribute("userAgent", userAgent);
        model.addAttribute("N", auth.getName());
        model.addAttribute("Auto", auth.getAuthorities());

        return "filler";

    } // */


    @GetMapping("/")
    @ResponseBody // Required else thymeleaf search for it's template
    public String getGithub(Principal user) {
        return "Welcome, ";//+ user.toString() + "\n http://localhost:8080/userInfo ";
    }


    /**
     * Show informations that can be fetched : DO NOT USE IT TO SHOW EVERYONE / HIDE CONFIDENTIAL DATA
     *
     * @param user
     * @return
     */
    @GetMapping("/userInfo")
    @ResponseBody // Required else thymeleaf search for it's template
    public String G(Principal user, @AuthenticationPrincipal OidcUser oidcUser) {
        return authentificationService.getUserInfo(user, oidcUser);
    }
}

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Controller
@CrossOrigin
@Slf4j
public class LoginController {


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

    /***
     * This endpoint will try to return as much information found on the user
     * !! This is not safe and can display private information !!
     *
     * @param request
     * @param authentication
     * @return
     */
    @GetMapping("/userDetails")
    @ResponseBody
    ResponseEntity<Map<String, Object>> getHomepage(HttpServletRequest request, Authentication authentication) {

        Map<String, Object> details = new HashMap<>();
        Map<String, Object> requestDetails = new HashMap<>();
        // Get User-Agent and Client IP
        requestDetails.put("userAgent", request.getHeader("User-Agent"));
        requestDetails.put("host", request.getHeader("host"));
        requestDetails.put("connection", request.getHeader("connection"));
        requestDetails.put("authorization", request.getHeader("authorization"));
        requestDetails.put("referer", request.getHeader("referer"));
        requestDetails.put("acceptLanguage", request.getHeader("accept-language"));
        requestDetails.put("platform", request.getHeader("sec-ch-ua-platform"));
        requestDetails.put("clientIp", request.getRemoteAddr());
        requestDetails.put("locale", request.getLocale().toString());
        requestDetails.put("localAddress", request.getLocalAddr());
        requestDetails.put("localName", request.getLocalName());
        requestDetails.put("localPort", String.valueOf(request.getLocalPort()));
        requestDetails.put("remoteUser", request.getRemoteUser());
        requestDetails.put("remoteHost", request.getRemoteHost());
        requestDetails.put("remoteAddress", request.getRemoteAddr());
        requestDetails.put("remotePort", String.valueOf(request.getRemotePort()));
        requestDetails.put("serverPort", String.valueOf(request.getServerPort()));
        requestDetails.put("serverName", request.getServerName());
        requestDetails.put("protocol", request.getProtocol());
        Stream<String> spliterator
                = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(request.getHeaderNames().asIterator(), Spliterator.ORDERED),
                false);
        requestDetails.put("requestHeaders", spliterator.collect(Collectors.joining(" || ")));
        details.put("requests", requestDetails);

        details.put("name", authentication.getName());
        details.put("autorization", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" -- ")));
        details.put("principal", authentication.getPrincipal().toString());

        details.put("authentification", authentificationService.getUserInfo(authentication));
        return ResponseEntity.ok(details);

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
}

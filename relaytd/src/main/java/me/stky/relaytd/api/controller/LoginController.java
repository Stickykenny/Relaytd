package me.stky.relaytd.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
public class LoginController {

    @RequestMapping("/login")
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

    }
}

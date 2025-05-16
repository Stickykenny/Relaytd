package me.stky.relaytd.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@Controller
public final class LoginController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;


    public LoginController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }


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


    @GetMapping("/")
    @ResponseBody // Required else thymeleaf search for it's template
    public String getGithub(Principal user) {
        return "Welcome, " + user.toString() + "\n http://localhost:8080/userInfo ";
    }


    private StringBuffer getUsernamePasswordLoginInfo(Principal user) {
        StringBuffer usernameInfo = new StringBuffer();

        UsernamePasswordAuthenticationToken token = ((UsernamePasswordAuthenticationToken) user);
        if (token.isAuthenticated()) {
            User u = (User) token.getPrincipal();
            usernameInfo.append("Welcome, " + u.getUsername());

            usernameInfo.append("<br> ALL , " + token.toString());
        } else {
            usernameInfo.append("NA");
        }
        return usernameInfo;
    }


    /**
     * Access Protected data do not show it, like the token d‘accès.
     *
     * @param user
     * @return
     */
    private StringBuffer getOAuth2LoginInfo(Principal user) {

        StringBuffer protectedInfo = new StringBuffer();

        OAuth2AuthenticationToken authToken = ((OAuth2AuthenticationToken) user);
        OAuth2AuthorizedClient authClient = this.authorizedClientService.loadAuthorizedClient(authToken.getAuthorizedClientRegistrationId(), authToken.getName());
        if (authToken.isAuthenticated()) {

            Map<String, Object> userAttributes = ((DefaultOAuth2User) authToken.getPrincipal()).getAttributes();


            // Null value if they are private on github
            protectedInfo.append("Welcome, " + userAttributes.get("name") + "<br><br>");
            protectedInfo.append("e-mail: " + userAttributes.get("email") + "<br><br>");

            // ACCESS TOKEN SHOULD NEVER APPEAR
            String userToken = authClient.getAccessToken().getTokenValue();
            protectedInfo.append("Access Token: " + userToken + "<br><br>");

            protectedInfo.append("ALL " + userAttributes.toString() + "<br><br>");

        } else {
            protectedInfo.append("NA");
        }
        return protectedInfo;


    }


    /**
     * Show informations that can be fetched : DO NOT USE IT TO SHOW EVERYONE / HIDE CONFIDENTIAL DATAz
     *
     * @param user
     * @return
     */
    @GetMapping("/userInfo")
    @ResponseBody // Required else thymeleaf search for it's template
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser) {

        StringBuffer userInfo = new StringBuffer();
        if (user instanceof UsernamePasswordAuthenticationToken) {
            userInfo.append(getUsernamePasswordLoginInfo(user));
        } else if (user instanceof OAuth2AuthenticationToken) {
            userInfo.append(getOAuth2LoginInfo(user));
        }
        return userInfo.toString();
    }
}

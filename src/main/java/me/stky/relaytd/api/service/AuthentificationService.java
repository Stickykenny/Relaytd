package me.stky.relaytd.api.service;

import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthentificationService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    public AuthentificationService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    private StringBuffer getUsernamePasswordLoginInfo(Principal user) {
        StringBuffer usernameInfo = new StringBuffer();

        UsernamePasswordAuthenticationToken token = ((UsernamePasswordAuthenticationToken) user);
        if (token.isAuthenticated()) {
            log.debug("Authenticated with Username Password");
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
            log.debug("Authenticated with OAuth2");
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

    public Map<String, String> getUserInfo(Principal user) {
        Map<String, String> authDetails = new HashMap<>();
        if (user instanceof UsernamePasswordAuthenticationToken) {
            System.out.println("session-based");
            authDetails.put("UsernamePasswordLoginInfo", (getUsernamePasswordLoginInfo(user).toString()));
        } else if (user instanceof OAuth2AuthenticationToken) {
            System.out.println("oauth");
            authDetails.put("OAuth2LoginInfo", (getOAuth2LoginInfo(user)).toString());
        } else if (user instanceof JwtAuthenticationToken) {
            System.out.println("jwt token");
            authDetails.put("name", user.getName());
            authDetails.put("authorities", ((JwtAuthenticationToken) user).getAuthorities().toString());
            authDetails.put("tokenAttributes", ((JwtAuthenticationToken) user).getTokenAttributes().toString());
        } else {
            throw new IllegalStateException("Login method not taken into account");
        }
        return authDetails;
    }

    /**
     * Retrieve username from Authentification
     * Google uses email, Github uses the url, InMemory and database uses username
     *
     * @param authentication
     * @return
     */
    public String fetchUsernameFromAuth(Authentication authentication) {

        Object principal = authentication.getPrincipal();
        String username = "";
        if (principal instanceof DefaultOidcUser) {
            // Google
            Map<String, Object> oidcUser = ((DefaultOidcUser) principal).getClaims();
            username = oidcUser.get("email").toString();
        } else if (principal instanceof DefaultOAuth2User) {
            // Github
            Map<String, Object> githubUser = ((DefaultOAuth2User) principal).getAttributes();
            username = githubUser.get("url").toString();
        } else if (principal instanceof User) {
            // User In Memory
            username = ((User) principal).getUsername();
        } else if (principal instanceof UserModel) {
            // User Database
            username = ((UserModel) principal).getUsername();
        } else {
            throw new IllegalStateException("Unexpected login method");
        }
        return username;
    }
}

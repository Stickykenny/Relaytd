package me.stky.relaytd.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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
            System.out.println("authenticated");
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
            System.out.println("authenticated");
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
}

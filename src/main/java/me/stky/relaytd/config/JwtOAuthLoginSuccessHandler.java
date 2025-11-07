package me.stky.relaytd.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.UserInfo;
import me.stky.relaytd.api.repository.UserRepository;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Success Handler happends when logging in with OAuth
 */

@Slf4j
public class JwtOAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    private final JWTService jwtService;

    public JwtOAuthLoginSuccessHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }


    public void saveGoogleOAuthCredential(OAuth2AuthenticationToken authentication, DefaultOidcUser principal) {
        Map<String, Object> oidcUser = principal.getClaims();
        String email = oidcUser.get("email").toString();
        String provider = authentication.getAuthorizedClientRegistrationId();
        String providerId = oidcUser.get("sub").toString();

        Optional<UserInfo> user = userRepository.findByUsername(email);
        if (user.isEmpty()) {
            UserInfo newCred = new UserInfo();
            newCred.setUsername(email);
            newCred.setProvider(provider);
            newCred.setProvider_id(providerId);
            String roles = Stream.of(Roles.ROLE_USER, Roles.ROLE_OAUTH_USER).map(Objects::toString).collect(Collectors.joining(","));
            newCred.setRoles(roles);

            log.info("Added a new credential from new OAuth login [Google] : " + newCred);
            saveUserInfoToDatabase(newCred);
        }
    }

    public void saveGithubOAuthCredential(OAuth2AuthenticationToken authentication, DefaultOAuth2User principal) {
        Map<String, Object> githubUser = principal.getAttributes();
        String url = githubUser.get("url").toString();
        String provider = authentication.getAuthorizedClientRegistrationId();
        String providerId = githubUser.get("id").toString();

        Optional<UserInfo> user = userRepository.findByUsername(url);
        if (user.isEmpty()) {
            UserInfo newCred = new UserInfo();
            newCred.setUsername(url);
            newCred.setProvider(provider);
            newCred.setProvider_id(providerId);
            String roles = Stream.of(Roles.ROLE_USER, Roles.ROLE_OAUTH_USER).map(Objects::toString).collect(Collectors.joining(","));
            newCred.setRoles(roles);

            log.info("Added a new credential from new OAuth login [Github] : " + newCred);
            saveUserInfoToDatabase(newCred);
        }
    }

    public void saveUserInfoToDatabase(UserInfo userInfo) {
        String randomValidationKey = jwtService.generateRandomValidationKey(128);
        userInfo.setValidation_key(randomValidationKey);
        log.debug("Generated this validation key [" + randomValidationKey + "], for this user [" + userInfo.getUsername() + "]");
        userRepository.save(userInfo);
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("Connecting using OAuth");

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.error("Authentification type wasn't OIDC. " + authentication);
            response.sendRedirect("http://localhost:4200/login/?login=failed");
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof DefaultOidcUser) && !(principal instanceof DefaultOAuth2User)) {
            log.info("This application only support Github and Google as external provider");
            log.error("Authentification type not recognized. " + principal);
            response.sendRedirect("http://localhost:4200/login?login=failed");
            return;
        }

        // Create Entry in DB
        if (principal instanceof DefaultOidcUser) {
            // Google
            saveGoogleOAuthCredential((OAuth2AuthenticationToken) authentication, (DefaultOidcUser) principal);
        } else if (principal instanceof DefaultOAuth2User) {
            // Github
            saveGithubOAuthCredential((OAuth2AuthenticationToken) authentication, (DefaultOAuth2User) principal);
        }

        String jwt = jwtService.generateToken(authentication);
        ResponseCookie cookie = jwtService.generateAccessCookie(jwt);
        log.debug(cookie.toString());

        ResponseCookie refreshCookie = jwtService.generateRefreshCookie(jwt);
        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Redirect to Angular frontend
        response.sendRedirect("http://localhost:4200/oauth-callback/");
    }
}

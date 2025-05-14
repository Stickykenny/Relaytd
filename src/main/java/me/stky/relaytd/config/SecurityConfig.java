package me.stky.relaytd.config;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import me.stky.relaytd.api.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private String jwtKey = "hahahahaCHANGETHISANDHIDEIT"; // See RSA key and have non symetrical


    //
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(this.jwtKey.getBytes(), 0, this.jwtKey.getBytes().length, "RSA");
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(this.jwtKey.getBytes()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user1 = User.withUsername("visitor")
                .password(passwordEncoder().encode("password"))
                .roles("VISITOR")
                .build();
        UserDetails admin = User.withUsername("visitor2")
                .password(passwordEncoder().encode("password"))
                .roles("NO_ROLE")
                .build();
        return new InMemoryUserDetailsManager(user1, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                //.csrf(csrf ->  csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository())) // cookie attack , avoid disabling it
                // authorize PUT and POST
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))

                .authorizeHttpRequests(

                        auth -> auth
                                .requestMatchers("/login", "/resources/**", "/static/**", "/css/**", "/js/**").permitAll()
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/user/**").hasAnyAuthority("USER", "ADMIN")
                                .anyRequest().authenticated()

                )

                // TODO debug for JWT token auth
                //.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // for token auth jwt
                //.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                //.httpBasic(Customizer.withDefaults()) // what's this one ?


                /*.formLogin(c -> c.loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/homepage", true)
                        .failureUrl("/login?error=true")
                )*/
                .formLogin(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults()) // method not found
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .build();

        // Use hasAuthority if data is "ADMIN"
        // Use hasRole if data is "ROLE_ADMIN"
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.parentAuthenticationManager(null); // avoid infinite loop on bad credential

        // In Memory
        authenticationManagerBuilder.inMemoryAuthentication().userDetailsPasswordManager(userDetailsService());
        authenticationManagerBuilder.userDetailsService(userDetailsService());


        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.getOrBuild();
    }


}
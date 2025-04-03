package me.stky.relaytd.config;


import me.stky.relaytd.api.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Doesn't work with db credential method
        UserDetails user1 = User.withUsername("u1")
                .password(passwordEncoder().encode("u1"))
                .roles("VISITOR")
                .build();
        UserDetails user2 = User.withUsername("u2")
                .password(passwordEncoder().encode("u2"))
                .roles("VISITOR")
                .build();
        UserDetails admin = User.withUsername("a")
                .password(passwordEncoder().encode("a"))
                .roles("VISITOR","USER")
                .build();
        return new InMemoryUserDetailsManager(user1, user2, admin);
    }
    */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(
                        auth -> auth
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/user/**").hasAnyAuthority("USER", "ADMIN")
                        .anyRequest().authenticated()

                )
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults()).build();

        // Use hasAuthority if data is "ADMIN"
        // Use hasRole if data is "ROLE_ADMIN"
        }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }


}
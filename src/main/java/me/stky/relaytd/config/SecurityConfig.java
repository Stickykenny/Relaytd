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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
                //.csrf(csrf -> csrf.disable())
                /*.csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
*/
                .authorizeHttpRequests(

                        auth -> auth
                                .requestMatchers("/login", "/resources/**", "/static/**", "/css/**", "/js/**").permitAll()
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/user/**").hasAnyAuthority("USER", "ADMIN")
                                .anyRequest().authenticated()

                )

                /*.formLogin(c -> c.loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/homepage", true)
                        .failureUrl("/login?error=true")
                )*/
                .formLogin(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults())
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
    public UserDetailsService users() {
        UserDetails user = User.builder().username("user").password(passwordEncoder().encode("password")).roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
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
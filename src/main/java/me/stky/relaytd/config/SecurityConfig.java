package me.stky.relaytd.config;


import com.nimbusds.jose.jwk.source.ImmutableSecret;
import me.stky.relaytd.api.service.AuthentificationService;
import me.stky.relaytd.api.service.CustomUserDetailsService;
import me.stky.relaytd.api.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {


    @Value("${spring.security.jwt.key}")
    private String jwtKey;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private AuthentificationService authentificationService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtLoginSuccessHandler jwtLoginSuccessHandler(JWTService jwtService) {
        return new JwtLoginSuccessHandler(jwtService);
    }


    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user1 = User.withUsername("visitor")
                .password(passwordEncoder().encode("password"))
                .roles(Roles.ROLE_USER.getAuthorityName())
                .build();
        UserDetails admin = User.withUsername("visitor2")
                .password(passwordEncoder().encode("password"))
                .roles(new String[]{Roles.ROLE_VISITOR.getAuthorityName(), "EMPTY"})
                .build();
        return new InMemoryUserDetailsManager(user1, admin);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(this.jwtKey.getBytes()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(this.jwtKey.getBytes(), 0, this.jwtKey.getBytes().length, "HmacSHA256");

        var decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // defaults to "scope" or "scp"
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // defaults to "SCOPE_"

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // SPA w/ JWT doesn't need csrf => csrf is for cookie / server-rendered pages
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // for JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login2").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/csrf/token").permitAll() // unused
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/auth/**", "/logout", "/resources/**", "/static/**", "/css/**").permitAll()
                        .anyRequest().authenticated())

                .oauth2Login(oauth -> oauth.successHandler(jwtLoginSuccessHandler(new JWTService(jwtEncoder(), authentificationService))))
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())) // Contains protected ressources // incompatible with formlogin
                // Authentification server : provide ID : ex : Github, FB, Google
                // Client Server is still Spring Boot - The Frontend calls the backend that ask the auth
                .httpBasic(Customizer.withDefaults()).build();

    }

    //server-side login
    /*@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                // authorize PUT and POST
                //.csrf(csrf -> csrf
                //        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                //        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS requests for handling CORS
                                .requestMatchers("/login", "/resources/**", "/static/**", "/css/**", "/js/**").permitAll()
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/user/**").hasAnyAuthority("USER", "ADMIN")
                                .anyRequest().authenticated()
                        //.anyRequest().permitAll()
                )
                //.formLogin(c -> c.loginPage("/login")
                //        .permitAll()
                //        .defaultSuccessUrl("/homepage", true)
                //       .failureUrl("/login?error=true")
                //)
                .formLogin(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
                .httpBasic(Customizer.withDefaults())
                .build();
        // Use hasAuthority if data is "ADMIN"
        // Use hasRole if data is "ROLE_ADMIN"
    } //*/

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.builder().username("user").password(passwordEncoder().encode("password")).roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /*@Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.parentAuthenticationManager(null); // avoid infinite loop on bad credential

        // In Memory
        authenticationManagerBuilder.inMemoryAuthentication().userDetailsPasswordManager(userDetailsService());
        authenticationManagerBuilder.userDetailsService(userDetailsService());


        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.getOrBuild();
    }*/

    /*
    // Configure is deprecated since 5.7 spring security
    https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
    @Bean
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.inMemoryAuthentication().userDetailsPasswordManager(userDetailsService());
        authenticationManagerBuilder.userDetailsService(userDetailsService());


        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
        //return authenticationManagerBuilder.getOrBuild();
    }*/

    @Bean
    public AuthenticationManager authenticationManager() {

        // DB auth
        DaoAuthenticationProvider dbProvider = new DaoAuthenticationProvider();
        dbProvider.setUserDetailsService(customUserDetailsService);
        dbProvider.setPasswordEncoder(new BCryptPasswordEncoder());

        // in-Memory auth
        DaoAuthenticationProvider inMemoryProvider = new DaoAuthenticationProvider();
        inMemoryProvider.setUserDetailsService(userDetailsService());
        inMemoryProvider.setPasswordEncoder(new BCryptPasswordEncoder());

        return new ProviderManager(List.of(inMemoryProvider, dbProvider)); // First In-memory
    }
}
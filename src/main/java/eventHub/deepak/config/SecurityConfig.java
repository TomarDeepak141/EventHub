package eventHub.deepak.config;

import eventHub.deepak.security.JwtAuthenticationFilter;
import eventHub.deepak.security.ApiSecurityErrorWriter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiSecurityErrorWriter errorWriter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ApiSecurityErrorWriter errorWriter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.errorWriter = errorWriter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/user/register", "/auth/login").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/events/mine").hasRole("ORGANIZER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/events/*/reviews/eligibility").hasRole("CUSTOMER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/events", "/events/*", "/events/*/availability", "/events/*/reviews").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/venues", "/venues/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/events/*/reviews").hasRole("CUSTOMER")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/reviews/**").hasRole("CUSTOMER")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/reviews/**").hasRole("CUSTOMER")
                        .requestMatchers("/wishlist/**").hasRole("CUSTOMER")
                        .requestMatchers("/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/events/*/reservations", "/events/*/reservations/**", "/events/*/bookings", "/events/*/bookings/**", "/bookings/**").hasRole("CUSTOMER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/tickets/verify").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/screens").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers("/events/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers("/venues/**", "/screens/**", "/seats/**", "/seat-categories/**", "/admin/**", "/user/admin/**").hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> errorWriter.write(request, response, org.springframework.http.HttpStatus.UNAUTHORIZED, "Authentication is required"))
                        .accessDeniedHandler((request, response, exception) -> errorWriter.write(request, response, org.springframework.http.HttpStatus.FORBIDDEN, "You are not allowed to perform this operation"))
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}

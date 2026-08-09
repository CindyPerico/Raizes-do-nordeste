package br.com.cindyperico.raizesdonordeste.config;

import br.com.cindyperico.raizesdonordeste.middleware.AccessAuditFilter;
import br.com.cindyperico.raizesdonordeste.security.JwtAuthenticationFilter;
import br.com.cindyperico.raizesdonordeste.security.RestAccessDeniedHandler;
import br.com.cindyperico.raizesdonordeste.security.RestAuthenticationEntryPoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String GERENTE = "GERENTE";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<AccessAuditFilter> accessAuditFilterRegistration(AccessAuditFilter filter) {
        FilterRegistrationBean<AccessAuditFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   AccessAuditFilter accessAuditFilter,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/h2-console/**", "/api/auth/login", "/api/auth/registrar").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/unidades/**", "/api/produtos/**").authenticated()
                        .requestMatchers("/api/unidades/**", "/api/produtos/**", "/api/funcionarios/**")
                        .hasAnyRole(ADMIN, GERENTE)
                        .requestMatchers("/api/relatorios/**").hasAnyRole(ADMIN, GERENTE)
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole(ADMIN)
                        .requestMatchers("/api/clientes/*/anonimizar").hasRole(ADMIN)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(accessAuditFilter, JwtAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

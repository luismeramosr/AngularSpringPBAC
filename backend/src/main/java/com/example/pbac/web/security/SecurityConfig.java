package com.example.pbac.web.security;

import com.example.pbac.persistence.service.security.UserService;
import com.example.pbac.util.config.Config;
import com.example.pbac.web.middleware.JwtAuthenticationFilter;
import com.example.pbac.web.security.provider.PasswordEncoderProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

/**
 * SecurityConfig es una clase de configuración que establece las reglas de
 * seguridad de la aplicación.
 * Configura la protección de las rutas HTTP, la autenticación mediante JWT, y
 * las políticas de CORS.
 * También define los componentes necesarios para la autenticación y
 * autorización del usuario.
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class SecurityConfig {
    private final Config config;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final PasswordEncoderProvider passwordEncoderProvider;

    /**
     * Configura los filtros de seguridad HTTP, las reglas de autorización y las
     * políticas de sesión.
     * Define las rutas que requieren autenticación y las que están permitidas sin
     * ella.
     *
     * @param http La configuración HTTP que se ajusta según las reglas de seguridad
     *             definidas.
     * @return La cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider()).addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration corsConfiguration = new CorsConfiguration();
                            corsConfiguration.addAllowedOrigin(config.origin);
                            corsConfiguration.addAllowedMethod("*");
                            corsConfiguration.addAllowedHeader("*");
                            corsConfiguration.addExposedHeader("Authorization");
                            corsConfiguration.setAllowCredentials(true);
                            return corsConfiguration;
                        }))
                .exceptionHandling(handling -> handling.accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }

    /**
     * Configura el proveedor de autenticación basado en DAO, utilizando el servicio
     * de detalles del usuario
     * y el codificador de contraseñas.
     *
     * @return El proveedor de autenticación configurado.
     */
    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(userService.userDetailsService());
        dao.setPasswordEncoder(passwordEncoderProvider.passwordEncoder());
        return dao;
    }

    /**
     * Configura el administrador de autenticación que gestiona el proceso de
     * autenticación de los usuarios.
     *
     * @param config La configuración de autenticación proporcionada por Spring
     *               Security.
     * @return El administrador de autenticación.
     * @throws Exception Si ocurre un error durante la configuración.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

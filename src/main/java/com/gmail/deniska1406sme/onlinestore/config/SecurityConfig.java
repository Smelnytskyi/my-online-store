package com.gmail.deniska1406sme.onlinestore.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthHandler oAuthHandler;
    private final PasswordAuthenticationHandler passwordAuthenticationHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired
    public SecurityConfig(OAuthHandler oAuthHandler,
                          PasswordAuthenticationHandler passwordAuthenticationHandler,
                          JwtTokenProvider jwtTokenProvider,
                          JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.oAuthHandler = oAuthHandler;
        this.passwordAuthenticationHandler = passwordAuthenticationHandler;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    // Автоконфигурация AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        AuthenticationEntryPoint customAuthEntryPoint = (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Authentication required\"}");
        };

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Разрешаем доступ ко всем статическим ресурсам, включая HTML-страницы
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/static/**", "/css/**", "/js/**").permitAll()  // Разрешаем доступ к статическим ресурсам

                        // Защищаем все API-эндпоинты
                        .requestMatchers("/admin/**", "/client/**", "/employee/**").authenticated()
                        .anyRequest().permitAll()
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthEntryPoint)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuthHandler)
                );

        return http.build();
    }
}

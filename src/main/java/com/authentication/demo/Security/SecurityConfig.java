package com.authentication.demo.Security;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
        public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieCustomizer() {
        return factory -> factory.addContextCustomizers(context ->
               context.setCookieProcessor(new Rfc6265CookieProcessor()));
        }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        // Only ignore CSRF for stateless API endpoints if needed
                        .ignoringRequestMatchers(
                                "/api/**"))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // <- This disables the blocking header
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/signup",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/search-live")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/index", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .build();
    }
}

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

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            Rfc6265CookieProcessor processor = new Rfc6265CookieProcessor();
            processor.setSameSiteCookies("None");
            context.setCookieProcessor(processor);
        });
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self' https://benlimpic.github.io/"))
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/signup", "/css/**", "/js/**", "/images/**", "/search-live").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .defaultSuccessUrl("/index", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> {
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("application/json")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/login?iframe=true");
                    }
                })
            )
            .build();
    }
}
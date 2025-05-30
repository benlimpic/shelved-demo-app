package com.authentication.demo.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.authentication.demo.Model.UserModel;
import com.authentication.demo.Repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@Profile("demo")
public class DemoSecurityConfig {

    private final UserRepository userRepository;

    public DemoSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/logout").denyAll() // ❌ block login/logout access
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src * 'unsafe-inline' 'unsafe-eval' data: blob:"))
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/index"); // ✅ redirect if blocked access
                })
            );

        // Auto-authenticate demo user
        http.addFilterBefore((request, response, chain) -> {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserModel demoUser = userRepository.findByUsername("demo")
                    .or(() -> userRepository.findByUsername("music-man"))
                    .orElse(null);

                if (demoUser != null) {
                    String[] roles = demoUser.getRoles() == null || demoUser.getRoles().isEmpty()
                        ? new String[]{"USER"}
                        : demoUser.getRoles().stream()
                            .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                            .toArray(String[]::new);

                    UserDetails userDetails = User.withUsername(demoUser.getUsername())
                        .password(demoUser.getPassword())
                        .roles(roles)
                        .build();

                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(request, response);
        }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

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
            System.out.println("🟢 DemoSecurityConfig is ACTIVE");
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src * 'unsafe-inline' 'unsafe-eval' data: blob:"))
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> res.setStatus(HttpServletResponse.SC_OK))
            );

        // Auto-authenticate the demo user on all requests
        http.addFilterBefore((req, res, chain) -> {
            UserModel demoUser = userRepository.findByUsername("music-man").orElse(null);
            if (demoUser == null) {
                throw new RuntimeException("Demo user not found. Make sure to seed it.");
            }

            UserDetails userDetails = User.withUsername(demoUser.getUsername())
                .password(demoUser.getPassword())
                .roles("USER")
                .build();

            var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(req, res);
        }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
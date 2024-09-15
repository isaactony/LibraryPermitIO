package com.permitioexample.LibraryPermitIO;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/h2-console/**").permitAll()  // Allow H2 Console access
                .requestMatchers("/books/**").hasAnyRole("ADMIN", "LIBRARIAN", "MEMBER")
                .anyRequest().authenticated()
                .and()
                .httpBasic();

        // To enable H2 Console (frame options)
        http.headers().frameOptions().disable();

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Set up in-memory users
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        UserDetails librarian = User.withDefaultPasswordEncoder()
                .username("librarian")
                .password("password")
                .roles("LIBRARIAN")
                .build();

        UserDetails member = User.withDefaultPasswordEncoder()
                .username("member")
                .password("password")
                .roles("MEMBER")
                .build();

        return new InMemoryUserDetailsManager(admin, librarian, member);
    }
}

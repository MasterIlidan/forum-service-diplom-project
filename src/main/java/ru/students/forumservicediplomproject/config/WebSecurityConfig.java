package ru.students.forumservicediplomproject.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {


    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests((authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        .requestMatchers("/").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/register/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/inactivePosts").permitAll() //убрать
                        .requestMatchers("/forum/*/deleteForum").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/forum/createForum").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/forum/saveForum").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/forum/*/createThread").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/forum/*/saveThread").hasAnyAuthority("ADMIN", "MODERATOR")
                        .requestMatchers("/approvePost/").hasAnyAuthority("ADMIN", "MODERATOR")
                        //.requestMatchers("/forum").permitAll()
                        .anyRequest().authenticated())).formLogin(form ->
                        form.loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/")
                                .permitAll())
                .logout(logout -> logout.logoutUrl("logout").permitAll()).csrf().disable();
        return httpSecurity.build();
    }
}

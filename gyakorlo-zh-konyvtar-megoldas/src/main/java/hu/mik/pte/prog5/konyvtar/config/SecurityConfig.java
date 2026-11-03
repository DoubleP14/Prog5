package hu.mik.pte.prog5.konyvtar.config;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import hu.mik.pte.prog5.konyvtar.ui.view.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration          // 2-2 (1. hiba): enelkul a Spring nem olvassa be az osztalyt
@EnableWebSecurity      // 2-1: webes security
@EnableMethodSecurity   // 8-1: global method security
public class SecurityConfig {

    // 2-1: a sajat LoginView legyen a bejelentkezo oldal
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin.loginView(LoginView.class));
        return http.build();
    }

    @Bean   // 2-2 (2. hiba): enelkul a felhasznalok nem kerulnek be a Springbe
    public UserDetailsManager userDetailsManager() {
        var admin = User.builder()
                .username("admin")
                .password("{noop}password")
                .roles("ADMIN", "EDITOR", "NOVEL", "CRIME", "SCIFI", "HISTORY", "POETRY")
                .build();
        var editor = User.builder()
                .username("editor")
                .password("{noop}password")   // 2-2 (3. hiba): hianyzott a {noop}
                .roles("EDITOR", "NOVEL", "CRIME")
                .build();
        var reader = User.builder()
                .username("reader")
                .password("{noop}password")
                .roles("NOVEL")
                .build();
        var scifan = User.builder()
                .username("scifan")
                .password("{noop}password")
                .roles("SCIFI", "HISTORY", "POETRY")
                .build();
        // 2-2 (4. hiba): az editor es a scifan nem volt atadva
        return new InMemoryUserDetailsManager(admin, editor, reader, scifan);
    }
}

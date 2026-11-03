package hu.mik.pte.prog5.filmtar.service;

import hu.mik.pte.prog5.filmtar.model.AppUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 3-2: a Spring Security ezen keresztul keresi meg a felhasznalot belepeskor
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final Map<String, AppUser> appUsers = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public AppUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.appUsers.put("user", new AppUser("user", passwordEncoder.encode("user"), Set.of("USER")));
        this.appUsers.put("admin", new AppUser("admin", passwordEncoder.encode("admin"), Set.of("USER", "ADMIN")));
    }

    // 9-1
    public boolean exists(String username) {
        return this.appUsers.containsKey(username);
    }

    // 9-1
    public void register(String username, String rawPassword) {
        AppUser user = new AppUser(username, this.passwordEncoder.encode(rawPassword), Set.of("USER"));
        if (this.appUsers.putIfAbsent(username, user) != null) {
            throw new IllegalStateException("User already exists!");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = this.appUsers.get(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return User.builder()
                .username(user.username())
                .password(user.passwordHash())
                .roles(user.roles().toArray(String[]::new))
                .build();
    }
}

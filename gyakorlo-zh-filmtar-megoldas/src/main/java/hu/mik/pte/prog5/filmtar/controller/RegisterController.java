package hu.mik.pte.prog5.filmtar.controller;

import hu.mik.pte.prog5.filmtar.model.RegisterForm;
import hu.mik.pte.prog5.filmtar.service.AppUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

// 9
@Controller
public class RegisterController {

    private final AppUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public RegisterController(AppUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                           BindingResult bindingResult,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        // 9-2: az annotacioval nem megoldhato ellenorzesek
        if (this.userDetailsService.exists(registerForm.getUsername())) {
            bindingResult.rejectValue("username", "validation.username.exists");
        }
        if (!Objects.equals(registerForm.getPassword(), registerForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "validation.password.mismatch");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        this.userDetailsService.register(registerForm.getUsername(), registerForm.getPassword());

        // 9-3: automatikus beleptetes
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(registerForm.getUsername());
        Authentication auth = UsernamePasswordAuthenticationToken
                .authenticated(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
        return "redirect:/movies";
    }
}

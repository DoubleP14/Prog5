package hu.mik.pte.prog5.filmtar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // 3-3: csak a GET a miénk, a POST /login kérést a Spring Security kezeli
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}

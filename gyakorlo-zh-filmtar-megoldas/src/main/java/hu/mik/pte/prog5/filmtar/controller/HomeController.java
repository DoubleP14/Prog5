package hu.mik.pte.prog5.filmtar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 5-1
    @GetMapping("/")
    public String home() {
        return "redirect:/movies";
    }
}

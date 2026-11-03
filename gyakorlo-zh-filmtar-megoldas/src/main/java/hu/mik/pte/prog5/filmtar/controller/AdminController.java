package hu.mik.pte.prog5.filmtar.controller;

import hu.mik.pte.prog5.filmtar.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final MovieService movieService;

    // 10-1: a jogosultsagot a SecurityConfig requestMatchers sora adja
    @GetMapping("/admin/stats")
    public String stats(Model model) {
        model.addAttribute("count", this.movieService.count());
        return "admin/stats";
    }
}

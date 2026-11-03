package hu.mik.pte.prog5.filmtar.controller;

import hu.mik.pte.prog5.filmtar.model.Movie;
import hu.mik.pte.prog5.filmtar.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // 5-1
    @GetMapping
    public String list(Model model) {
        model.addAttribute("movies", this.movieService.list());
        return "movies/list";
    }

    // 6-2
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "movies/form";
    }

    // 6-2: a BindingResult kozvetlenul a @Valid parameter utan kell alljon
    @PostMapping
    public String create(@Valid @ModelAttribute("movie") Movie movie, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "movies/form";
        }
        this.movieService.save(movie);
        return "redirect:/movies";
    }

    // 6-3
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Movie movie = this.movieService.findById(id);
        if (movie == null) {
            return "redirect:/movies";
        }
        model.addAttribute("movie", movie);
        return "movies/form";
    }

    // 6-3: az urlapon nincs tulajdonos, ezert a regi filmbol vesszuk at
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("movie") Movie movie,
                         BindingResult bindingResult) {
        Movie existing = this.movieService.findById(id);
        if (existing == null) {
            return "redirect:/movies";
        }
        movie.setId(id);
        if (bindingResult.hasErrors()) {
            return "movies/form";
        }
        movie.setOwner(existing.getOwner());
        this.movieService.save(movie);
        return "redirect:/movies";
    }

    // 7-1
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        this.movieService.deleteById(id);
        return "redirect:/movies";
    }
}

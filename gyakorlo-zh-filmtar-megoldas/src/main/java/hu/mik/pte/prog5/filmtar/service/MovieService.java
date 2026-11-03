package hu.mik.pte.prog5.filmtar.service;

import hu.mik.pte.prog5.filmtar.model.Movie;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final Map<Long, Movie> movies = new LinkedHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    // 2-2: az aktiv profil dönti el, melyik implementacio jon ide
    private final DataService dataService;

    // 2-2: indulaskor meg nincs bejelentkezett felhasznalo, ezert nem a save()-et hivjuk
    @PostConstruct
    private void init() {
        this.dataService.getValues().forEach(movie -> {
            movie.setId(this.idSeq.incrementAndGet());
            this.movies.put(movie.getId(), movie);
        });
    }

    // 11-1 (bonusz): a sajat filmek, az ADMIN mindent lat. Modosithato lista kell neki, ezert ArrayList.
    @PostFilter("filterObject.owner == authentication.name or hasRole('ADMIN')")
    public Collection<Movie> list() {
        return new ArrayList<>(this.movies.values());
    }

    // 11-2 (bonusz): idegen filmet csak ADMIN tolthet be
    @PostAuthorize("returnObject == null or returnObject.owner == authentication.name or hasRole('ADMIN')")
    public Movie findById(Long id) {
        return this.movies.get(id);
    }

    // 2-2: uj filmnel azonositot es tulajdonost kap
    public Movie save(Movie movie) {
        if (movie.getId() == null) {
            movie.setId(this.idSeq.incrementAndGet());
            movie.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());
        }
        this.movies.put(movie.getId(), movie);
        return movie;
    }

    // 7-2
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        this.movies.remove(id);
    }

    // 10-1
    public int count() {
        return this.movies.size();
    }
}

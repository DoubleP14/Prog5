package hu.mik.pte.prog5.filmtar.service;

import hu.mik.pte.prog5.filmtar.model.Movie;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

// 2-1: csak a "mock" profillal jon letre
@Service
@Profile("mock")
public class InMemoryMockDataService implements DataService {

    @Override
    public Collection<Movie> getValues() {
        return List.of(
                new Movie("Dűne", "Denis Villeneuve", 2021, "user"),
                new Movie("Eredet", "Christopher Nolan", 2010, "user"),
                new Movie("Mátrix", "Lana Wachowski", 1999, "admin"),
                new Movie("Kontroll", "Antal Nimród", 2003, "admin"));
    }
}

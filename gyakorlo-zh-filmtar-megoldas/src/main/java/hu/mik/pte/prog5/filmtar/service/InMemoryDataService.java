package hu.mik.pte.prog5.filmtar.service;

import hu.mik.pte.prog5.filmtar.model.Movie;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

// 2-1: csak a "plain" profillal jon letre, ures listaval indul
@Service
@Profile("plain")
public class InMemoryDataService implements DataService {

    @Override
    public Collection<Movie> getValues() {
        return List.of();
    }
}

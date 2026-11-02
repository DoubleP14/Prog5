package hu.mik.pte.prog5.zh1.repository;

import hu.mik.pte.prog5.zh1.entity.Pet;
import hu.mik.pte.prog5.zh1.entity.PetSpecies;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("db")
public class DBPetRepository implements PetRepository {
    @Override
    public List<Pet> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pet> findByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pet> findBySpecies(PetSpecies species) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pet save(Pet pet) {
        throw new UnsupportedOperationException();
    }
}

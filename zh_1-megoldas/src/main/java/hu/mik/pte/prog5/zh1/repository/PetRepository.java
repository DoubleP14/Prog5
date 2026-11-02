package hu.mik.pte.prog5.zh1.repository;

import hu.mik.pte.prog5.zh1.entity.Pet;
import hu.mik.pte.prog5.zh1.entity.PetSpecies;

import java.util.List;

public interface PetRepository {

    List<Pet> findAll();

    List<Pet> findByName(String name);

    List<Pet> findBySpecies(PetSpecies species);

    Pet save(Pet pet);

}

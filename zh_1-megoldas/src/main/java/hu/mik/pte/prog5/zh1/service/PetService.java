package hu.mik.pte.prog5.zh1.service;

import hu.mik.pte.prog5.zh1.entity.Pet;
import hu.mik.pte.prog5.zh1.entity.PetSpecies;

import java.util.List;

public interface PetService {

    List<Pet> listAll();

    Pet save(Pet pet);

    List<PetSpecies> listSpecies();
}

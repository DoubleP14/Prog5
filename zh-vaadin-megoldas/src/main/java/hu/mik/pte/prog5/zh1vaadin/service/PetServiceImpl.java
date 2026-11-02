package hu.mik.pte.prog5.zh1vaadin.service;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import hu.mik.pte.prog5.zh1vaadin.repository.PetRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    // 3: az interface-re hivatkozunk, a "mock" nevu implementaciot a @Qualifier valasztja ki
    private final PetRepository petRepository;

    public PetServiceImpl(@Qualifier("mock") PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // 8-3 (bonusz): csak azok az allatok maradnak meg, amelyek fajanak megfelelo szerepe van a usernek.
    // filterObject = a lista aktualis eleme (Pet); species enum, a szerepek String-ek -> .name()
    @Override
    @PostFilter("@securityService.getRolesAsString().contains(filterObject.species.name())")
    public List<Pet> listAll() {
        return this.petRepository.findAll();
    }

    // 8-2: csak REGISTER jogosultsaggal hivhato
    @Override
    @PreAuthorize("hasRole('REGISTER')")
    public Pet save(Pet pet) {
        return this.petRepository.save(pet);
    }

    @Override
    @PreAuthorize("hasRole('REGISTER')")
    public List<PetSpecies> listSpecies() {
        return Arrays.asList(PetSpecies.values());
    }
}

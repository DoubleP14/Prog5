package hu.mik.pte.prog5.zh1vaadin.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import hu.mik.pte.prog5.zh1vaadin.service.PetService;
import hu.mik.pte.prog5.zh1vaadin.ui.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = MainLayout.class)  // 6-2
@RolesAllowed("REGISTER")                               // 8-2: csak REGISTER joggal latogathato
public class RegisterView extends VerticalLayout implements HasDynamicTitle {

    private final PetService petService;

    private final Binder<Pet> binder = new Binder<>(Pet.class);

    TextField name = new TextField(this.getTranslation("pet.name"));
    ComboBox<PetSpecies> species = new ComboBox<>(this.getTranslation("pet.species"));

    public RegisterView(PetService petService) {
        this.petService = petService;

        // 6-1: a fajok a service-bol jonnek, a megjelenitett szoveg a forditasbol
        this.species.setItems(this.petService.listSpecies());
        this.species.setItemLabelGenerator(s -> this.getTranslation("pet.species." + s.name()));

        // 6-1: bindolas + kotelezo mezok (asRequired -> piros pottyot is kirakja a cimke melle)
        this.binder.forField(this.name)
                .asRequired(this.getTranslation("register.required"))
                .bind(Pet::getName, Pet::setName);
        this.binder.forField(this.species)
                .asRequired(this.getTranslation("register.required"))
                .bind(Pet::getSpecies, Pet::setSpecies);

        Button submit = new Button(this.getTranslation("register.submit"), e -> this.save());

        this.add(this.name, this.species, submit);
    }

    private void save() {
        Pet pet = new Pet();
        // csak akkor ir a pet-be, ha minden validacio sikeres; kulonben a mezok pirosak lesznek
        if (this.binder.writeBeanIfValid(pet)) {
            this.petService.save(pet);
            UI.getCurrent().navigate(ListView.class);
        }
    }

    @Override
    public String getPageTitle() {
        return this.getTranslation("title.pet.register");
    }
}

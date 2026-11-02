package hu.mik.pte.prog5.zh1.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import hu.mik.pte.prog5.zh1.entity.Pet;
import hu.mik.pte.prog5.zh1.entity.PetSpecies;
import hu.mik.pte.prog5.zh1.service.PetService;
import hu.mik.pte.prog5.zh1.ui.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = MainLayout.class)  // 5-2
@RolesAllowed("REGISTER")                               // 7-2
public class RegisterView extends VerticalLayout implements HasDynamicTitle {

    private final PetService petService;

    private final Binder<Pet> binder = new Binder<>(Pet.class);

    private final TextField name = new TextField(this.getTranslation("pet.name"));
    private final RadioButtonGroup<PetSpecies> species = new RadioButtonGroup<>(this.getTranslation("pet.species"));

    public RegisterView(PetService petService) {
        this.petService = petService;

        // 5-1: fajok a service-bol, felirat a forditasbol, egymas ala (mint a kepen)
        this.species.setItems(this.petService.listSpecies());
        this.species.setItemLabelGenerator(s -> this.getTranslation("pet.species." + s.name()));
        this.species.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        // 5-1: bindolas + kotelezo mezok
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
        if (this.binder.writeBeanIfValid(pet)) {
            this.petService.save(pet);
            Notification.show(this.getTranslation("register.success"), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(ListView.class);
        } else {
            // 5-1: hiba eseten figyelmezteto uzenet a feluletre
            Notification.show(this.getTranslation("register.invalid"), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public String getPageTitle() {
        return this.getTranslation("title.pet.register");
    }
}

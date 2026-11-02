package hu.mik.pte.prog5.zh1.ui.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import hu.mik.pte.prog5.zh1.entity.Pet;
import hu.mik.pte.prog5.zh1.service.PetService;
import hu.mik.pte.prog5.zh1.service.SecurityService;
import hu.mik.pte.prog5.zh1.ui.MainLayout;
import jakarta.annotation.security.PermitAll;

@Route(value = "list", layout = MainLayout.class)   // 4-2
@RouteAlias(value = "", layout = MainLayout.class)  // belepes utan "/"-re visz a Vaadin
@PermitAll
public class ListView extends VerticalLayout implements HasDynamicTitle {

    private final Grid<Pet> grid = new Grid<>(Pet.class, false);

    public ListView(PetService petService, SecurityService securityService) {
        // 4-1: Identifier, Name, Species
        var idColumn = this.grid.addColumn(Pet::getUid).setHeader(this.getTranslation("pet.id"));
        this.grid.addColumn(Pet::getName).setHeader(this.getTranslation("pet.name"));
        this.grid.addColumn(pet -> this.getTranslation("pet.species." + pet.getSpecies().name()))
                .setHeader(this.getTranslation("pet.species"));

        // azonosito csak ADMIN-nak
        idColumn.setVisible(securityService.getRolesAsString().contains("ADMIN"));

        this.grid.setItems(petService.listAll());

        this.setSizeFull();
        this.add(this.grid);
    }

    @Override
    public String getPageTitle() {
        return this.getTranslation("title.pet.list");
    }
}

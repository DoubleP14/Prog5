package hu.mik.pte.prog5.zh1vaadin.ui.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.service.PetService;
import hu.mik.pte.prog5.zh1vaadin.service.SecurityService;
import hu.mik.pte.prog5.zh1vaadin.ui.MainLayout;
import jakarta.annotation.security.PermitAll;

@Route(value = "list", layout = MainLayout.class)   // 5-2
@RouteAlias(value = "", layout = MainLayout.class)  // belepes utan a "/"-re iranyit a Vaadin, ott is ez jelenjen meg
@PermitAll                                          // barmelyik bejelentkezett user
public class ListView extends VerticalLayout implements HasDynamicTitle {

    // false: ne generaljon automatikusan oszlopokat, mi adjuk meg a sorrendet
    private final Grid<Pet> grid = new Grid<>(Pet.class, false);

    public ListView(PetService petService, SecurityService securityService) {
        // 5-1: oszlopok sorrendje: Azonosito, Nev, Faj
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
